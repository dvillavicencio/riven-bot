package com.danielvm.destiny2bot.dto.destiny.character.vaultitems;

import com.danielvm.destiny2bot.dto.destiny.DataResponse;
import lombok.Data;

@Data
public class VaultData implements DataResponse {

    private ListVaultItems data;
}
