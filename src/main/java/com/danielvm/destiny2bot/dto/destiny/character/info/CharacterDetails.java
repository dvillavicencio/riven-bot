package com.danielvm.destiny2bot.dto.destiny.character.info;

import com.fasterxml.jackson.annotation.JsonAlias;

public record CharacterDetails(@JsonAlias("Response") Characters response) {
}
