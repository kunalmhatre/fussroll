package com.fussroll.fussroll;

import java.util.Arrays;

/**
 * Created by kunal on 2/2/17.
 */

class CategoryLogMetaHelper {

    private String category, meta, logHere;

    CategoryLogMetaHelper(String category, String log, String meta) {
        this.category = category;
        this.logHere = log;
        this.meta = meta;
    }

    boolean validate() {

        if(Arrays.asList(CategoryLogMeta.categories).indexOf(category.substring(0,1).toUpperCase()+category.substring(1, category.length())) != -1) {

            boolean validData = false;
            switch (category) {
                case "feeling":
                    if(Arrays.asList(CategoryLogMeta.feeling).indexOf(logHere) != -1) {
                        if(Arrays.asList(CategoryLogMeta.getMetaForFeeling()).indexOf(meta) != -1)
                            validData = true;;
                    }
                    break;
                case "activity":
                    if(Arrays.asList(CategoryLogMeta.activity).indexOf(logHere) != -1)
                        validData = true;
                    break;
                case "place":
                    if(Arrays.asList(CategoryLogMeta.place).indexOf(logHere) != -1) {
                        if(Arrays.asList(CategoryLogMeta.getMetaForPlace(logHere)).indexOf(meta) != -1)
                            validData = true;
                    }
                    break;
                case "food":
                    if(Arrays.asList(CategoryLogMeta.food).indexOf(logHere) != -1) {
                        if(Arrays.asList(CategoryLogMeta.getMetaForFood(logHere)).indexOf(meta) != -1)
                            validData = true;
                    }
                    break;
                case "drink":
                    if(Arrays.asList(CategoryLogMeta.drink).indexOf(logHere) != -1) {
                        if(Arrays.asList(CategoryLogMeta.getMetaForDrink(logHere)).indexOf(meta) != -1)
                            validData = true;
                    }
                    break;
                case "game":
                    if(Arrays.asList(CategoryLogMeta.game).indexOf(logHere) != -1) {
                        if(Arrays.asList(CategoryLogMeta.getMetaForGame(logHere)).indexOf(meta) != -1)
                            validData = true;;
                    }
                    break;
                default:
                    validData = false;
            }

            return validData;

        }
        else
            return false;

    }

    int getImageID() {

        if(validate()) {
            switch (category) {
                case "feeling":
                    return CategoryLogMeta.feelingIcons[Arrays.asList(CategoryLogMeta.feeling).indexOf(logHere)];
                case "activity":
                    return CategoryLogMeta.activityIcons[Arrays.asList(CategoryLogMeta.activity).indexOf(logHere)];
                case "place":
                    return CategoryLogMeta.placeIcons[Arrays.asList(CategoryLogMeta.place).indexOf(logHere)];
                case "food":
                    return CategoryLogMeta.foodIcons[Arrays.asList(CategoryLogMeta.food).indexOf(logHere)];
                case "drink":
                    return CategoryLogMeta.drinkIcons[Arrays.asList(CategoryLogMeta.drink).indexOf(logHere)];
                case "game":
                    return CategoryLogMeta.gameIcons[Arrays.asList(CategoryLogMeta.game).indexOf(logHere)];
                default:
                    return 0;
            }
        }
        else
            return 0;

    }

}
