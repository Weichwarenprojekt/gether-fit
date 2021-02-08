pub mod google {
    use std::time::{Duration, SystemTime, UNIX_EPOCH};

    use jsonwebtoken::{decode, DecodingKey, Validation};
    use serde::{Deserialize, Serialize};

    #[derive(Debug, Deserialize, Serialize)]
    pub struct Token {
        pub name: String,
        pub email: String,
        pub given_name: String,
        pub picture: String,
    }

    #[derive(Debug, Deserialize, Serialize)]
    pub struct Jwt {
        kid: String,
        kty: String,
        r#use: String,
        pub n: String,
        pub e: String,
        alg: String,
    }

    #[derive(Debug, Deserialize, Serialize)]
    pub struct Jwts {
        pub keys: Vec<Jwt>,
    }

    pub struct LoginValidation {
        expiration: Duration,
        keys: Vec<DecodingKey<'static>>,
        validation: Validation,
    }

    impl LoginValidation {
        pub fn new(validation: Validation) -> LoginValidation {
            LoginValidation {
                expiration: Duration::from_secs(0),
                keys: vec![],
                validation,
            }
        }

        async fn update_google_jwt(&mut self) {
            if let Ok(resp) = reqwest::get("https://www.googleapis.com/oauth2/v3/certs").await {
                if let Ok(jwts) = serde_json::from_str::<Jwts>(
                    resp.text()
                        .await
                        .unwrap_or_else(|_| "invalid".to_string())
                        .as_str(),
                ) {
                    self.keys = jwts
                        .keys
                        .iter()
                        .map(|j| DecodingKey::from_rsa_components(&j.n, &j.e).into_static())
                        .collect();

                    self.expiration = SystemTime::now()
                        .duration_since(UNIX_EPOCH)
                        .expect("Time went backwards")
                        + Duration::from_secs(2 * 60 * 60);
                }
            }
        }

        pub async fn validate(&mut self, google_token: &str) -> Option<Token> {
            if SystemTime::now()
                .duration_since(UNIX_EPOCH)
                .expect("Time went backwards")
                > self.expiration
            {
                self.update_google_jwt().await;
            }

            for jwt in self.keys.iter() {
                if let Ok(token_data) = decode::<Token>(google_token, &jwt, &self.validation) {
                    return Some(token_data.claims);
                }
            }

            return None;
        }
    }
}
