package com.danielvm.destiny2bot.dto.destiny.manifest;

import lombok.Data;

@Data
public class DisplayPropertiesDto {

    /**
     * Description for display properties
     */
    private String description;

    /**
     * The name for display properties
     */
    private String name;

    /**
     * The icon path (if any)
     */
    private String icon;

    /**
     * The high resolution icon path (if any)
     */
    private String highResIcon;

    /**
     * If current manifest entity has an icon
     */
    private Boolean hasIcon;
}
