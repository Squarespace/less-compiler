
2.x Function Checklist
---------

Misc 
* [default]
* [svg-gradient]

String

* [replace] - adding to extension package, due to unconstrained regex evaluation.

Type

* [isruleset]

Color Channel

* [hsvhue]
* [hsvsaturation]
* [hsvvalue]



NodeJs-specific
------
The following upstream features only work in the Node.js environment. They may
be added later as extensions:

* [data-uri]
* [image-size]
* [image-width]
* [image-height]


Completed, verify no changes are needed.
-----

Misc

* [color]
* [convert]
* [get-unit]
* [unit]

String

* [escape]
* [e]
* [format](%)

List

* [length]
* [extract]

Math

* [abs]
* [acos]
* [asin]
* [atan]
* [ceil]
* [cos]
* [floor]
* [mod]
* [min]
* [max]
* [percentage]
* [pi]
* [pow]
* [round]
* [sin]
* [sqrt]
* [tan]

Type 

* [isnumber]
* [isstring]
* [iscolor]
* [iskeyword]
* [isurl]
* [ispixel]
* [isem]
* [ispercentage]
* [isunit]

Color Definition

* [rgb]
* [rgba]
* [argb]
* [hsl]
* [hsla]
* [hsv]
* [hsva]

Color Channel

* [hue]
* [saturation]
* [lightness]
* [red]
* [green]
* [blue]
* [alpha]
* [luma]
* [luminance]
 
Color Operation

* [saturate]
* [desaturate]
* [lighten]
* [darken]
* [fadein]
* [fadeout]
* [fade]
* [spin]
* [greyscale]
* [contrast]

Color Blending

* [multiply]
* [screen]
* [overlay]
* [softlight]
* [hardlight]
* [difference]
* [exclusion]
* [average]
* [negation]
* [mix]

Deprecated Color Blending

* shade
* tint



[color]: http://lesscss.org/functions/#misc-functions-color
[convert]: http://lesscss.org/functions/#misc-functions-convert
[data-uri]: http://lesscss.org/functions/#misc-functions-data-uri
[default]: http://lesscss.org/functions/#misc-functions-default
[get-unit]: http://lesscss.org/functions/#misc-functions-get-unit
[image-height]: http://lesscss.org/functions/#misc-functions-image-height
[image-size]: http://lesscss.org/functions/#misc-functions-image-size
[image-width]: http://lesscss.org/functions/#misc-functions-image-width
[svg-gradient]: http://lesscss.org/functions/#misc-functions-svg-gradient
[unit]: http://lesscss.org/functions/#misc-functions-unit

[escape]: http://lesscss.org/functions/#string-functions-escape
[e]: http://lesscss.org/functions/#string-functions-e
[format]: http://lesscss.org/functions/#string-functions--format
[replace]: http://lesscss.org/functions/#string-functions-replace

[ceil]: http://lesscss.org/functions/#math-functions-ceil
[floor]: http://lesscss.org/functions/#math-functions-floor
[percentage]: http://lesscss.org/functions/#math-functions-percentage
[round]: http://lesscss.org/functions/#math-functions-round

[length]: http://lesscss.org/functions/#list-functions-length
[extract]: http://lesscss.org/functions/#list-functions-extract

[sqrt]: http://lesscss.org/functions/#math-functions-sqrt
[abs]: http://lesscss.org/functions/#math-functions-abs
[sin]: http://lesscss.org/functions/#math-functions-sin
[asin]: http://lesscss.org/functions/#math-functions-asin
[cos]: http://lesscss.org/functions/#math-functions-cos
[acos]: http://lesscss.org/functions/#math-functions-acos
[tan]: http://lesscss.org/functions/#math-functions-tan
[atan]: http://lesscss.org/functions/#math-functions-atan
[pi]: http://lesscss.org/functions/#math-functions-pi
[pow]: http://lesscss.org/functions/#math-functions-pow
[mod]: http://lesscss.org/functions/#math-functions-mod
[min]: http://lesscss.org/functions/#math-functions-min
[max]: http://lesscss.org/functions/#math-functions-max

[isunit]: http://lesscss.org/functions/#type-functions-isunit
[isruleset]: http://lesscss.org/functions/#type-functions-isruleset
[isnumber]: http://lesscss.org/functions/#type-functions-isnumber
[isstring]: http://lesscss.org/functions/#type-functions-isstring
[iscolor]: http://lesscss.org/functions/#type-functions-iscolor
[iskeyword]: http://lesscss.org/functions/#type-functions-iskeyword
[isurl]: http://lesscss.org/functions/#type-functions-isurl
[ispixel]: http://lesscss.org/functions/#type-functions-ispixel
[isem]: http://lesscss.org/functions/#type-functions-isem
[ispercentage]: http://lesscss.org/functions/#type-functions-ispercentage

[rgb]: http://lesscss.org/functions/#color-definition-rgb
[rgba]: http://lesscss.org/functions/#color-definition-rgba
[argb]: http://lesscss.org/functions/#color-definition-argb
[hsl]: http://lesscss.org/functions/#color-definition-hsl
[hsla]: http://lesscss.org/functions/#color-definition-hsla
[hsv]: http://lesscss.org/functions/#color-definition-hsv
[hsva]: http://lesscss.org/functions/#color-definition-hsva

[hue]: http://lesscss.org/functions/#color-channel-hue
[saturation]: http://lesscss.org/functions/#color-channel-saturation
[lightness]: http://lesscss.org/functions/#color-channel-lightness
[hsvhue]: http://lesscss.org/functions/#color-channel-hsvhue
[hsvsaturation]: http://lesscss.org/functions/#color-channel-hsvsaturation
[hsvvalue]: http://lesscss.org/functions/#color-channel-hsvvalue
[red]: http://lesscss.org/functions/#color-channel-red
[green]: http://lesscss.org/functions/#color-channel-green
[blue]: http://lesscss.org/functions/#color-channel-blue
[alpha]: http://lesscss.org/functions/#color-channel-alpha
[luma]: http://lesscss.org/functions/#color-channel-luma
[luminance]: http://lesscss.org/functions/#color-channel-luminance

[saturate]: http://lesscss.org/functions/#color-operation-saturate
[desaturate]: http://lesscss.org/functions/#color-operation-desaturate
[lighten]: http://lesscss.org/functions/#color-operation-lighten
[darken]: http://lesscss.org/functions/#color-operation-darken
[fadein]: http://lesscss.org/functions/#color-operation-fadein
[fadeout]: http://lesscss.org/functions/#color-operation-fadeout
[fade]: http://lesscss.org/functions/#color-operation-fade
[spin]: http://lesscss.org/functions/#color-operation-spin
[mix]: http://lesscss.org/functions/#color-operation-mix
[greyscale]: http://lesscss.org/functions/#color-operation-greyscale
[contrast]: http://lesscss.org/functions/#color-operation-contrast

[multiply]: http://lesscss.org/functions/#color-blending-multiply
[screen]: http://lesscss.org/functions/#color-blending-screen
[overlay]: http://lesscss.org/functions/#color-blending-overlay
[softlight]: http://lesscss.org/functions/#color-blending-softlight
[hardlight]: http://lesscss.org/functions/#color-blending-hardlight
[difference]: http://lesscss.org/functions/#color-blending-difference
[exclusion]: http://lesscss.org/functions/#color-blending-exclusion
[average]: http://lesscss.org/functions/#color-blending-average
[negation]: http://lesscss.org/functions/#color-blending-negation

