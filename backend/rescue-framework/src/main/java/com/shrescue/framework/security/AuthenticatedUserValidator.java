package com.shrescue.framework.security;

/**
 * Refreshes the authorization information carried by a token.  Keeping this
 * contract in the framework lets the system module revoke disabled accounts
 * and role changes without trusting a 24-hour-old JWT payload.
 */
public interface AuthenticatedUserValidator {

    LoginUser validate(LoginUser tokenUser);
}
