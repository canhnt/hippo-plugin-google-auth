# Google Authentication Plugin for Hippo CMS 11

# Configuration
## Login page
Add/modify values to the configuration path at `/hippo:configuration/hippo:frontend/login/login/loginPage`
* Property: `plugin.class`
  * Type: `String`
  * Value: `org.onehippo.forge.googleauth.cms.GoogleLoginPlugin`
* Property: `google.signin.clientid`
  * Type: `String`
  * Value: `your google app client-id`
* Property: `google.signin.scope`
  * Type: `String`
  * Value: `profile`

## Security provider
Create a new security provider at `/hippo:configuration/hippo:security/`
```
+ google-signin (hipposys:securityprovider)
  - hipposys:classname (String): org.onehippo.forge.googleauth.repository.GoogleSignInSecurityProvider
```
