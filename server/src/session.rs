use std::fmt::Write;

use rand::{thread_rng, Rng};
use rocket::{
    http::{Cookie, CookieJar, Status},
    request::{FromRequest, Outcome},
    tokio::sync::Mutex,
    Request, State,
};
use sha2::{Digest, Sha256};
use std::collections::HashMap;

pub fn generate_session_id() -> Vec<u8> {
    let random_bytes = thread_rng().gen::<[u8; 32]>();

    let result = Sha256::digest(&random_bytes);

    result.to_vec()
}

pub fn to_hex_string(val: Vec<u8>) -> String {
    let mut s = String::with_capacity(2 * val.len());
    for b in val {
        write!(s, "{:02x}", b).unwrap();
    }

    s
}

pub struct SessionId {
    pub user: String,
}

#[rocket::async_trait]
impl<'a, 'r> FromRequest<'a, 'r> for SessionId {
    type Error = ();

    async fn from_request(request: &'a Request<'r>) -> Outcome<Self, Self::Error> {
        let cookies = request.guard::<&CookieJar>().await.unwrap();
        match cookies.get("id") {
            None => Outcome::Failure((Status::Unauthorized, ())),
            Some(crumb) => {
                let ids = request.guard::<State<'_, SessionData>>().await.unwrap();
                let ids = ids.ids.lock().await;

                if let Some(name) = ids.get(&crumb.value().to_string()) {
                    Outcome::Success(SessionId {
                        user: name.to_string(),
                    })
                } else {
                    cookies.remove(Cookie::named("id"));
                    Outcome::Failure((Status::Unauthorized, ()))
                }
            }
        }
    }
}

pub struct SessionData {
    pub ids: Mutex<HashMap<String, String>>,
}
