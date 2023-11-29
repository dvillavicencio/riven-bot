package com.danielvm.destiny2bot.dto.destiny.membership;

import com.fasterxml.jackson.annotation.JsonAlias;

public record MembershipResponse(@JsonAlias("Response") Memberships response) {

}
