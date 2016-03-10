package com.helpshift.res.drawable;

import android.content.Context;
import java.util.HashMap;
import java.util.Map;

public final class HSImages {
    public static Map<String, String> imagesMap = new HashMap();

    static {
        imagesMap.put("newHSLogo", "iVBORw0KGgoAAAANSUhEUgAAAGsAAAANAgMAAADTfHjYAAAACVBMVEX///8AAAD///9+749PAAAAAnRSTlMAAHaTzTgAAABjSURBVHhezc3RCcAwCEXRLPkE8x0LzT4KGUCoW9bEGQp94NflYPvTglTNo5tjEoElpOtkMX+asSyDMTFEVzaVvGwYLbIdx5gQdN/u7m4+mh63ykk5UDklvcjt/MNuMRlR7YO9P3E9S4qoPpwAAAAASUVORK5CYII=");
    }

    private static void setImageResources(String densityId) {
        if (densityId.equals("ldpi")) {
            imagesMap.put("newHSLogo", "iVBORw0KGgoAAAANSUhEUgAAAFAAAAAKCAYAAAAjBNk8AAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAAAeRJREFUeNrslk8ow2EYxzdzUIQTJyHaVasVaikOjiIlwkXIVSx/Wg4ONGIlFyU3+VOs5LAcKCnbCQepbUJKSWorIlk/36e+q7e3tT8uUr+nPv3e533ev8/7fd/NahiGxbTfm9VMoJnAP7U8EAeSxSal/gFcs+wAfpadYJ/lEPsN0v+i76C/Qn+P/hX9kRTr6GEsSr8AHIIiIuVCrc8OqATjYIhxg4hNAbfS3s+YT6kbZV2IvuytBMyBdtAMVhl7ZVs7qGY5Lgk8ESWCMZDPjQ+AaeAFEeIB/eCJAz6DKrAOGsABx5llXDbUBTqBDTwyLm07tGTUcP5WsA0+uXAPkfJ7BjHUgk3OIVaqxROgnIKRA2sEdWxfn+mmgjN+w+AOrMkB5THLks0l8M3Tz8ZkMfdUYBC0cZwZxmXDu1RgIsNYt5z/CHSzLgA+SCBFnwTnX2R/UW+vosCY1t7GQxeFbYFzcMP2ZYryY1RvVOkrbVz8tuhXOKnA0xyvv65AHxVrVxSgKjCd6QrMxmyc383+2SjQxWubNK+yBwuVL/3mOV4qBR7rCdRtGGzwHZgAb3wTi6nQ5RQKjHATk6CP8RdNgRU8waDypqZTYK6mK/ASLIALRUWqOVkXVt5x81fY/Bvzz+xHgAEAP2CQXW6MsVoAAAAASUVORK5CYII=");
        } else if (densityId.equals("hdpi")) {
            imagesMap.put("newHSLogo", "iVBORw0KGgoAAAANSUhEUgAAAKEAAAAUCAYAAADlVl1wAAAABHNCSVQICAgIfAhkiAAAAAlwSFlzAAAK6wAACusBgosNWgAAABx0RVh0U29mdHdhcmUAQWRvYmUgRmlyZXdvcmtzIENTNui8sowAAAAUdEVYdENyZWF0aW9uIFRpbWUAMi81LzEzLFqQkQAAAQtJREFUaIHtmMsOhCAMRe1k/v+XOysSYlppFWicnLNT+wKuBRVVPQAq+VQXAIAIoRxECOUgQijnGzESkdV1wJ8S+fClE0I5iBDKQYRQTuhMOAvtDghyOmi2Z/19VVXv2orlxc/YRmu+qs2K0ecdjcGbG6vWUYxs7siczGarCI8jN1ENb4Gv7p19MrYROxGRdj3yvyLqF52rJ7mfjuUu20VodbyZsbz4T/J6vplF67vMnbosu5bf8vfy3cm9mrJOuCpWvzCz8s7oPNaWl6nLsxt1/isxVouvsV2EFt4bPdryMh0q0zXPrOwYM+paVdsuJPIz8cXjg2L4WQ2vABFCOaHtGGAldEIoBxFCOT+s0QeQrsDu7QAAAABJRU5ErkJggg==");
        } else if (densityId.equals("xhdpi")) {
            imagesMap.put("newHSLogo", "iVBORw0KGgoAAAANSUhEUgAAANYAAAAaAgMAAAC2p0pyAAAACVBMVEUAAAD///////9zeKVjAAAAAnRSTlMAAHaTzTgAAAB4SURBVHhe7ZExCsQwEMRUz+vS5H+q55VXGHyG62zINVGzsKxAsNw7XA9rf+alEhFTKjUFUkIAQ804K2be7WkGgxowSBCCKHM15tAg7Gp1aDNyaIUAtN9IaTE90GSJdI0MaySQGXmiRRJSzPIAAsyVUI+1X17uHa4PK1nGXdiNcBIAAAAASUVORK5CYII=");
        } else if (densityId.equals("xxhdpi")) {
            imagesMap.put("newHSLogo", "iVBORw0KGgoAAAANSUhEUgAAAUIAAAAoCAYAAACLtfMUAAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAAAU9JREFUeNrs3MGugjAQBVAx/v8vVzdsCFCmMKHUc3Yv9EFb8DqZGKZSygvgn71tASAIAQQhgCAEEIQAghBAEAIIQgBBCCAIAQQhgCAEEIQAghBAEAIIQgBBCPBvPgOvbfXV29PP7j8tXtm9NX4eFz1eNl4J3jou+7zVTW6cZ+vx2jy21nv2PkSfm9bnLTqP7HWvDVURAqgInyfrm/VsJdM6rnadrPNedb757+Xxs9e/6vm4+3nrZd0792nInFARAirC0Rd4tOfX67yi879rvdHrZlWCJViyXL1fR8+3XH/r/R3tc6EiBFAR5uj1my7ayztaCfTSc+ql51fbt+we4dX7Fl3f0z4XKkIAFWEfFVprjyfa88rqqWX1Hmue2nPqZb9UaipCgHsLoVF/FwSgIgQQhACCEEAQAghCAEEIIAgBBCGAIAQQhACCEGDXV4ABANMB/qjZ05tAAAAAAElFTkSuQmCC");
        }
    }

    public static void initImages(Context c) {
        switch (c.getResources().getDisplayMetrics().densityDpi) {
            case 120:
                setImageResources("ldpi");
                return;
            case 160:
                setImageResources("mdpi");
                return;
            case 240:
                setImageResources("hdpi");
                return;
            case 320:
                setImageResources("xhdpi");
                return;
            case 480:
                setImageResources("xxhdpi");
                return;
            default:
                return;
        }
    }
}
