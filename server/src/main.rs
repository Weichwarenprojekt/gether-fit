#[macro_use]
extern crate rocket;
#[macro_use]
extern crate rocket_contrib;

use crate::login::google::LoginValidation;
use crate::session::{generate_session_id, to_hex_string, SessionData, SessionId};
use jsonwebtoken::{Algorithm, Validation};
use rocket::{
    http::{Cookie, CookieJar, Status},
    request::LenientForm,
    response::status::BadRequest,
    tokio::{fs::File, sync::Mutex},
    Request, State,
};
use rocket_contrib::databases::diesel;
use serde::Serialize;
use std::collections::HashMap;
mod login;
mod session;

#[get("/getcookie")]
async fn get_cookie(cookiejar: &CookieJar<'_>) -> String {
    cookiejar
        .get("id")
        .map_or("no cookie found".to_string(), |e| e.value().to_string())
}

#[get("/delcookie")]
async fn del_cookie(cookiejar: &CookieJar<'_>) -> String {
    cookiejar.remove(Cookie::named("id"));
    String::from("")
}

#[get("/testcookie")]
async fn test_cookie(sess_id: SessionId) -> String {
    format!("You're logged in as {}", sess_id.user)
}

#[get("/onetap")]
async fn one_tap_login() -> Option<File> {
    File::open("static/onetap.html").await.ok()
}

#[derive(FromForm, Serialize)]
struct GoogleData {
    credential: String,
    g_csrf_token: String,
}

struct LoginValidationData {
    login_validation: Mutex<LoginValidation>,
}

#[post("/login", data = "<google>")]
async fn google_login(
    google: LenientForm<GoogleData>,
    cookies: &CookieJar<'_>,
    sess_data: State<'_, SessionData>,
    login_validation_data: State<'_, LoginValidationData>,
) -> Result<(), BadRequest<()>> {
    let token;
    {
        token = login_validation_data
            .login_validation
            .lock()
            .await
            .validate(google.credential.as_str())
            .await;
    }

    if let Some(token) = token {
        let session_id = to_hex_string(generate_session_id());

        {
            let mut ids = sess_data.ids.lock().await;
            ids.insert(session_id.to_string(), token.email);
        }

        cookies.add(
            Cookie::build("id", session_id)
                .http_only(true)
                // todo secure cookie in production
                .finish(),
        );

        Ok(())
    } else {
        Err(BadRequest(None))
    }
}

#[catch(401)]
async fn unauthorized(status: Status, request: &Request<'_>) -> String {
    request.cookies().remove(Cookie::named("id"));
    format!("{}: {}", status.code, status.reason)
}

#[database("sqlite_logs")]
struct LogsDbConn(diesel::SqliteConnection);

#[rocket::main]
async fn main() {
    let validation: Validation = Validation {
        iss: Some(String::from("https://accounts.google.com")),
        aud: Some(
            vec![
                "626106044667-eocia2ru5jv9ar6n8ue5n9gjnkfl10sk.apps.googleusercontent.com"
                    .to_string(),
            ]
            .into_iter()
            .collect(),
        ),
        ..Validation::new(Algorithm::RS256)
    };

    let login_validation = LoginValidation::new(validation);

    rocket::ignite()
        .attach(LogsDbConn::fairing())
        .mount(
            "/",
            routes![
                one_tap_login,
                get_cookie,
                del_cookie,
                test_cookie,
                google_login
            ],
        )
        .register(catchers!(unauthorized))
        .manage(SessionData {
            ids: Mutex::new(HashMap::new()),
        })
        .manage(LoginValidationData {
            login_validation: Mutex::new(login_validation),
        })
        .launch()
        .await
        .unwrap();
}
