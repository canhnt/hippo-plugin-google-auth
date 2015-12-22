# Google Authentication Plugin for Hippo

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

## Logout menu
Add the property to the configuration path at `/hippo:configuration/hippo:frontend/cms/cms-static/root`
* Property: `usermenu.class`
* Type: `String`
* Value: `org.onehippo.forge.googleauth.cms.GoogleLogoutUserMenu`

## Security provider
Configure the security provider at `/hippo:configuration/hippo:security/internal`
* Property: `hipposys:classname`
* Type: `String`
* Value: `org.onehippo.forge.googleauth.repository.GoogleSignInSecurityProvider`