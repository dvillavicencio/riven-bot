package com.danielvm.destiny2bot.dto.destiny.manifest;

import com.fasterxml.jackson.annotation.JsonAlias;

public record ManifestEntity(@JsonAlias("Response") ResponseFields response) {
}
