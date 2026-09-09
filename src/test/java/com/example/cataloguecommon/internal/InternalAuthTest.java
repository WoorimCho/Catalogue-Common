package com.example.cataloguecommon.internal;

import com.example.cataloguecommon.internal.InternalAuth.InvalidInternalAuthException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InternalAuthTest {

    private static final String SECRET = "test-secret";

    @Test
    void roundTripsAnActingUser() {
        String h = InternalAuth.header(SECRET, "PUT", "/api/accounts/7", 7L);
        Long actingUser = InternalAuth.verify(SECRET, h, "PUT", "/api/accounts/7");
        assertThat(actingUser).isEqualTo(7L);
    }

    @Test
    void roundTripsNoUser() {
        String h = InternalAuth.header(SECRET, "POST", "/api/accounts", null);
        assertThat(InternalAuth.verify(SECRET, h, "POST", "/api/accounts")).isNull();
    }

    @Test
    void rejectsWrongSecret() {
        String h = InternalAuth.header(SECRET, "POST", "/api/ingredients", null);
        assertThatThrownBy(() -> InternalAuth.verify("other-secret", h, "POST", "/api/ingredients"))
                .isInstanceOf(InvalidInternalAuthException.class).hasMessageContaining("signature");
    }

    @Test
    void rejectsMethodOrPathTampering() {
        String h = InternalAuth.header(SECRET, "GET", "/api/ingredients/1", null);
        assertThatThrownBy(() -> InternalAuth.verify(SECRET, h, "DELETE", "/api/ingredients/1"))
                .isInstanceOf(InvalidInternalAuthException.class);
        assertThatThrownBy(() -> InternalAuth.verify(SECRET, h, "GET", "/api/ingredients/2"))
                .isInstanceOf(InvalidInternalAuthException.class);
    }

    @Test
    void rejectsMissingMalformedAndStale() {
        assertThatThrownBy(() -> InternalAuth.verify(SECRET, null, "GET", "/x"))
                .isInstanceOf(InvalidInternalAuthException.class).hasMessageContaining("missing");
        assertThatThrownBy(() -> InternalAuth.verify(SECRET, "garbage", "GET", "/x"))
                .isInstanceOf(InvalidInternalAuthException.class).hasMessageContaining("malformed");
        String stale = "t=1000,u=-,s=" + "0".repeat(64);
        assertThatThrownBy(() -> InternalAuth.verify(SECRET, stale, "GET", "/x"))
                .isInstanceOf(InvalidInternalAuthException.class).hasMessageContaining("stale");
    }
}
