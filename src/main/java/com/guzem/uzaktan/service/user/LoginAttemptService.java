package com.guzem.uzaktan.service.user;

/**
 * Tracks login failure/success to enforce account lockout policy.
 * Separated from UserService so that LoginEventListener doesn't depend on
 * the full user-management contract.
 */
public interface LoginAttemptService {

    void recordFailure(String email);

    void recordSuccess(String email);
}
