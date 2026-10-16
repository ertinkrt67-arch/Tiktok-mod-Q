
from __future__ import annotations

import os
import re
import struct
import subprocess
from typing import Dict, List, Optional, Tuple

TELEPHONY = "Landroid/telephony/TelephonyManager;"
REGION = "Lbk/soufit/feed/Region;"
ACCENT = "Lbk/soufit/theme/Accent;"
DOWNLOAD = "Lbk/soufit/media/Download;"
FEED = "Lbk/soufit/feed/Feed;"

VIDEO = "Lcom/ss/android/ugc/aweme/feed/model/Video;"
ACL = "Lcom/ss/android/ugc/aweme/feed/model/ACLCommonShare;"
AWEME = "Lcom/ss/android/ugc/aweme/feed/model/Aweme;"
USER = "Lcom/ss/android/ugc/aweme/profile/model/User;"
PROFILE_USER = "Lcom/ss/android/ugc/profile/platform/base/data/UserProfileInfo;"
VIDEO_CONTROL = "Lcom/ss/android/ugc/aweme/feed/model/VideoControl;"
FEED_ITEM_LIST = "Lcom/ss/android/ugc/aweme/feed/model/FeedItemList;"
PHOTO_IMAGE = "Lcom/ss/android/ugc/aweme/feed/model/PhotoModeImageUrlModel;"
ACCOUNT_SERVICE = "Lcom/ss/android/ugc/aweme/IAccountUserService;"
ACCOUNT = "Lbk/soufit/profile/Account;"
COMMENTS = "Lbk/soufit/feed/Comments;"
RAIL = "Lbk/soufit/feed/FeedRail;"
RAIL_CLASSES = (
    "Lcom/ss/android/ugc/aweme/feed/assem/digg/VideoDiggAssem;",
    "Lcom/ss/android/ugc/aweme/feed/assem/videocomment/VideoCommentAssem;",
    "Lcom/ss/android/ugc/aweme/feed/favorite/VideoFavoriteAssem;",
    "Lcom/ss/android/ugc/aweme/feed/assem/share/VideoShareAssem;",
)
PLAYER_CONTROLLER="Lcom/ss/android/ugc/aweme/feed/controller/PlayerController;"
REPOST_CLASS="Lcom/ss/android/ugc/aweme/upvote/detail/videorepost/VideoRepostAssem;"
AVATAR_CLASS="Lcom/ss/android/ugc/aweme/feed/assem/avatar/FeedAvatarDefaultAssem;"
PROFILE_CONTAINER="Lcom/ss/android/ugc/profile/platform/base/component/BaseContainerComponent;"
MESSAGE_CLASSES=("Lcom/ss/android/ugc/aweme/im/sdk/chat/messagelist/skeleton/cell/IMSkeletonLayoutReusedUISlotAssem;","Lcom/ss/android/ugc/aweme/im/sdk/chat/messagelist/skeleton/cell/SkeletonLayoutReusedUISlotAssem;")
PUSH_GUIDE="Lcom/ss/android/ugc/aweme/unifiedauth/push/UniAuthGuidePopupServiceImpl;"
FRIENDS_RESPONSE="Lcom/ss/android/ugc/aweme/relation/model/FriendsListResponse;"
COMMENT_IMAGE = "Lcom/ss/android/ugc/aweme/comment/model/CommentImageStruct;"
COMMENT = "Lcom/ss/android/ugc/aweme/comment/model/Comment;"
COMMENT_STICKER = "Lcom/ss/android/ugc/aweme/comment/model/CommentStickerStruct;"
WATERMARK = "Lbk/soufit/media/Watermark;"
FLAGS = "Lbk/soufit/core/Flags;"
SOUND = "Lbk/soufit/feed/Sound;"
BADGE = "Lbk/soufit/profile/Badge;"
AVATARS = "Lbk/soufit/media/Avatars;"
STICKERS = "Lbk/soufit/media/Stickers;"
STREAKS = "Lbk/soufit/social/Streaks;"
STREAK_DATA = "Lcom/ss/android/ugc/aweme/im/streak/api/StreakData;"
STREAK_SERVICE = "Lcom/ss/android/ugc/aweme/im/streak/api/IStreakService;"
STICKER_ITEM = "Lcom/ss/android/ugc/aweme/im/common/model/StickerItem;"
STICKER_IMAGE = "Lcom/ss/android/ugc/aweme/im/common/model/StickerImage;"
STICKER_BASE = "Lcom/ss/android/ugc/aweme/im/common/model/StickerBase;"
STICKER_CLICK = "Lcom/ss/android/ugc/aweme/im/messagelist/api/ability/MessageListStickerClickAbility;"
STICKER_TEMPLATE = "Lcom/ss/android/ugc/aweme/im/message/template/card/StickerTemplate;"
TEXT_VIEW = "Landroid/widget/TextView;"
TUX_TEXT = "Lcom/bytedance/tux/input/TuxTextView;"
CHAR_SEQUENCE = "Ljava/lang/CharSequence;"
SEEKBAR = "Lbk/soufit/feed/Seekbar;"
FONTS = "Lbk/soufit/theme/Fonts;"
RATE = "Lbk/soufit/feed/Rate;"
TYPEFACE = "Landroid/graphics/Typeface;"
PAINT_CLASS = "Landroid/graphics/Paint;"
MUSIC = "Lcom/ss/android/ugc/aweme/music/model/Music;"

WILD_SOURCES: List[Tuple[str, str, str, str]] = [
    ("setSeekBarShowType", "(I)V", "(Ljava/lang/Object;I)V", SEEKBAR),
]

_TAP = "Landroid/view/View;LX/1AUi;LX/0RoP;" + STICKER_TEMPLATE
_SHEET = ("Lcom/bytedance/lighten/loader/SmartImageView;"
          "Landroidx/fragment/app/FragmentManager;LX/0RoP;" + STICKER_TEMPLATE)
_CORNER = "Lcom/ss/android/ugc/aweme/views/RoundingCornerLayout;" + STICKER_TEMPLATE
_ANY = "Ljava/lang/Object;"

WILD_SOURCES += [
    ("RQ1", "(%s)V" % _TAP, "(%s%s)V" % (_ANY * 4, STICKER_TEMPLATE), STICKERS),
    ("rP", "(%s)V" % _TAP, "(%s%s)V" % (_ANY * 4, STICKER_TEMPLATE), STICKERS),
    ("DD2", "(%s)V" % _SHEET, "(%s%s)V" % (_ANY * 4, STICKER_TEMPLATE), STICKERS),
    ("vQ1", "(%s)V" % _CORNER, "(%s%s)V" % (_ANY * 2, STICKER_TEMPLATE), STICKERS),
]

SETTINGS_MANAGER = "Lcom/bytedance/ies/abmock/SettingsManager;"
AB_READERS: List[Tuple[str, str, str]] = [
    ("LIZ", "Ljava/lang/String;Z", "Z"),
    ("LJ", "Ljava/lang/String;I", "I"),
    ("LJFF", "Ljava/lang/String;J", "J"),
    ("LJI", "Ljava/lang/String;Ljava/lang/String;", "Ljava/lang/String;"),
    ("LIZJ", "Ljava/lang/String;F", "F"),
    ("LIZIZ", "Ljava/lang/String;D", "D"),
]
CANVAS = "Landroid/graphics/Canvas;"

COMMENT_TAP = ("(Landroid/view/View;%s ZLjava/lang/String;Ljava/util/Map;"
               "Ljava/lang/String;)V" % COMMENT_STICKER).replace("; Z", ";Z")

DISCOVERED_STATICS: List[Tuple[str, str, str, str]] = [
    ("comment sticker tapped", COMMENT_TAP, "stickerTapped", COMMENTS),
]

STICKER_ITEM_D = "Lcom/ss/android/ugc/aweme/im/common/model/StickerItem;"
FUNCTION0 = "Lkotlin/jvm/functions/Function0;"

COMMENT_SHEET = ("(Ljava/lang/String;%s Landroid/view/View;ZLjava/lang/String;"
                 "Ljava/util/Map;%s %s %s)V"
                 % (STICKER_ITEM_D, FUNCTION0, FUNCTION0, FUNCTION0)).replace(" ", "")

DISCOVERED_VIRTUALS: List[Tuple[str, str, str, str]] = [
    ("comment sticker sheet", COMMENT_SHEET, "stickerSheet", COMMENTS),
]

FOUND: Dict[str, Tuple[str, str]] = {}


VIEW = "Landroid/view/View;"
LONG_CLICK = "Landroid/view/View$OnLongClickListener;"

ANCHORED_SOURCES: List[Tuple[str, str, str, str, str, str]] = [
    ("bindSticker: ", VIEW, "setOnLongClickListener",
     "(%s)V" % LONG_CLICK, "(%s%s)V" % (VIEW, LONG_CLICK), COMMENTS),
    ("Lcom/ss/android/ugc/aweme/commentv2/commentlist/powercell/BaseCommentCell;",
     VIEW, "setOnLongClickListener", "(%s)V" % LONG_CLICK,
     "(%s%s)V" % (VIEW, LONG_CLICK), COMMENTS),
    ("[tiktok_logo]", CANVAS, "drawBitmap",
     "(Landroid/graphics/Bitmap;FFLandroid/graphics/Paint;)V",
     "(%sLandroid/graphics/Bitmap;FFLandroid/graphics/Paint;)V" % CANVAS, WATERMARK),
]

CLASS_ANCHORED_SOURCES = {
    "Lcom/ss/android/ugc/aweme/commentv2/commentlist/powercell/BaseCommentCell;",
}

SPEED_TOUCH_CLASS = "LY/ATListenerS452S0100000_16;"
SPEED_EDGE_CLASS = "LX/16wu;"
SPEED_EDGE_METHOD = ".method public final onTouch(Landroid/view/View;Landroid/view/MotionEvent;)Z"
SPEED_TOUCH_METHOD = (
    ".method public static final onTouch$8(LY/ATListenerS452S0100000_16;"
    "Landroid/view/View;Landroid/view/MotionEvent;)Z"
)
SPEED_TOUCH_TARGET = (
    "Lbk/soufit/feed/SpeedGesture;->onTouch(Ljava/lang/Object;"
    "Landroid/view/View;Landroid/view/MotionEvent;)Z"
)
SPEED_TOUCH_HOOK = """\
    invoke-static {p0, p1, p2}, Lbk/soufit/feed/SpeedGesture;->onTouch(Ljava/lang/Object;Landroid/view/View;Landroid/view/MotionEvent;)Z
    move-result v0
    if-eqz v0, :soufit_speed_touch_continue
    const/4 v0, 0x1
    return v0
:soufit_speed_touch_continue
"""
STRING = "Ljava/lang/String;"
URL_MODEL = "Lcom/ss/android/ugc/aweme/base/model/UrlModel;"
BOXED_BOOLEAN = "Ljava/lang/Boolean;"
LIST = "Ljava/util/List;"

SUBSCRIPTIONS = "Landroid/telephony/SubscriptionManager;"

MODEL_SOURCES: List[Tuple[str, str, str, str, str]] = [
    (TELEPHONY, "getPhoneCount", "()I", "(%s)I" % TELEPHONY, REGION),
    (TELEPHONY, "getActiveModemCount", "()I", "(%s)I" % TELEPHONY, REGION),
    (TELEPHONY, "getPhoneType", "()I", "(%s)I" % TELEPHONY, REGION),
    (SUBSCRIPTIONS, "getActiveSubscriptionInfoCount", "()I",
     "(%s)I" % SUBSCRIPTIONS, REGION),
    (VIDEO, "getDownloadAddr", "()%s" % URL_MODEL, "(%s)%s" % (VIDEO, URL_MODEL), DOWNLOAD),
    (ACL, "getCode", "()I", "(%s)I" % ACL, DOWNLOAD),
    (ACL, "getShowType", "()I", "(%s)I" % ACL, DOWNLOAD),
    (ACL, "getTranscode", "()I", "(%s)I" % ACL, DOWNLOAD),
    (AWEME, "isPreventDownload", "()Z", "(%s)Z" % AWEME, DOWNLOAD),
    (USER, "isPreventDownload", "()Z", "(%s)Z" % USER, DOWNLOAD),
    (FEED_ITEM_LIST, "getItems", "()%s" % LIST, "(%s)%s" % (FEED_ITEM_LIST, LIST), FEED),
    (ACCOUNT_SERVICE, "getCurUser", "()%s" % USER,
     "(%s)%s" % (ACCOUNT_SERVICE, USER), ACCOUNT),
    (ACCOUNT_SERVICE, "getCurUserId", "()%s" % STRING,
     "(%s)%s" % (ACCOUNT_SERVICE, STRING), ACCOUNT),
    (ACCOUNT_SERVICE, "getCurSecUserId", "()%s" % STRING,
     "(%s)%s" % (ACCOUNT_SERVICE, STRING), ACCOUNT),
    (COMMENT, "getStickerStruct", "()%s" % COMMENT_STICKER,
     "(Ljava/lang/Object;)%s" % COMMENT_STICKER, COMMENTS),
    (COMMENT_IMAGE, "getCropUrl", "()%s" % URL_MODEL,
     "(%s)%s" % (COMMENT_IMAGE, URL_MODEL), COMMENTS),
    (COMMENT_IMAGE, "getOriginUrl", "()%s" % URL_MODEL,
     "(%s)%s" % (COMMENT_IMAGE, URL_MODEL), COMMENTS),
    (USER, "getNickname", "()Ljava/lang/String;",
     "(%s)Ljava/lang/String;" % USER, BADGE),
    ("Lcom/ss/android/ugc/aweme/im/contacts/api/model/IMUser;", "getDisplayName", "()Ljava/lang/String;",
     "(Ljava/lang/Object;)Ljava/lang/String;", BADGE),
    ("Lcom/ss/android/ugc/aweme/im/common/model/BaseContact;", "getNickName", "()Ljava/lang/String;",
     "(Ljava/lang/Object;)Ljava/lang/String;", BADGE),
    ("Lcom/ss/android/ugc/aweme/im/contacts/api/model/IMContact;", "getDisplayName", "()Ljava/lang/String;",
     "(Ljava/lang/Object;)Ljava/lang/String;", BADGE),
    ("Lcom/ss/android/ugc/aweme/im/contacts/api/model/IMUser;", "getNickName", "()Ljava/lang/String;",
     "(Ljava/lang/Object;)Ljava/lang/String;", BADGE),
    (PROFILE_USER, "getNickname", "()Ljava/lang/String;",
     "(%s)Ljava/lang/String;" % PROFILE_USER, BADGE),

    (USER, "getAvatarLarger", "()%s" % URL_MODEL, "(%s)%s" % (USER, URL_MODEL), AVATARS),
    (USER, "getAvatar300", "()%s" % URL_MODEL, "(%s)%s" % (USER, URL_MODEL), AVATARS),
    (USER, "getAvatarMedium", "()%s" % URL_MODEL, "(%s)%s" % (USER, URL_MODEL), AVATARS),
    (TEXT_VIEW, "setTypeface", "(%s)V" % TYPEFACE,
     "(%s%s)V" % (TEXT_VIEW, TYPEFACE), FONTS),
    (TEXT_VIEW, "setTypeface", "(%sI)V" % TYPEFACE,
     "(%s%sI)V" % (TEXT_VIEW, TYPEFACE), FONTS),
    (TUX_TEXT, "setTypeface", "(%s)V" % TYPEFACE,
     "(%s%s)V" % (TEXT_VIEW, TYPEFACE), FONTS),
    (TUX_TEXT, "setTypeface", "(%sI)V" % TYPEFACE,
     "(%s%sI)V" % (TEXT_VIEW, TYPEFACE), FONTS),
    (PAINT_CLASS, "setTypeface", "(%s)%s" % (TYPEFACE, TYPEFACE),
     "(%s%s)%s" % (PAINT_CLASS, TYPEFACE, TYPEFACE), FONTS),

    ("Landroid/view/Surface;", "setFrameRate", "(FI)V",
     "(Landroid/view/Surface;FI)V", RATE),
    ("Landroid/view/Surface;", "setFrameRate", "(FII)V",
     "(Landroid/view/Surface;FII)V", RATE),

    (TEXT_VIEW, "setText", "(%s)V" % CHAR_SEQUENCE,
     "(%s%s)V" % (TEXT_VIEW, CHAR_SEQUENCE), BADGE),
    (TUX_TEXT, "setText", "(%s)V" % CHAR_SEQUENCE,
     "(%s%s)V" % (TEXT_VIEW, CHAR_SEQUENCE), BADGE),
    (TUX_TEXT, "setText", "(%sLandroid/widget/TextView$BufferType;)V" % CHAR_SEQUENCE,
     "(%s%sLandroid/widget/TextView$BufferType;)V" % (TEXT_VIEW, CHAR_SEQUENCE), BADGE),
    (TEXT_VIEW, "setText", "(%sLandroid/widget/TextView$BufferType;)V" % CHAR_SEQUENCE,
     "(%s%sLandroid/widget/TextView$BufferType;)V" % (TEXT_VIEW, CHAR_SEQUENCE), BADGE),


    (STREAK_SERVICE, ("LJIJJLI", "streakOf"), "(%sZ)%s" % (STRING, STREAK_DATA),
     "(%s%sZ)%s" % (STREAK_SERVICE, STRING, STREAK_DATA), STREAKS),
    (STREAK_SERVICE, ("LJJJI", "showsStreak"), "(%sZ)Z" % STRING,
     "(%s%sZ)Z" % (STREAK_SERVICE, STRING), STREAKS),

    (MUSIC, "available", "()Z", "(%s)Z" % MUSIC, SOUND),
    (MUSIC, "getMusicStatus", "()I", "(%s)I" % MUSIC, SOUND),
    (MUSIC, "isMuteShare", "()Z", "(%s)Z" % MUSIC, SOUND),
    (MUSIC, "getMuteType", "()I", "(%s)I" % MUSIC, SOUND),
]

MODEL_STATICS: List[Tuple[str, str, str, str, str]] = [
    (SETTINGS_MANAGER, (name, "flag"), "(%s)%s" % (args, kind),
     "(%s)%s" % (args, kind), FLAGS)
    for name, args, kind in AB_READERS
]

DATES = "Lbk/soufit/feed/Dates;"
ANCHORED_SOURCES.append(("LX/0HCj;", "Landroid/widget/ImageView;", "setImageDrawable",
    "(Landroid/graphics/drawable/Drawable;)V",
    "(Landroid/widget/ImageView;Landroid/graphics/drawable/Drawable;)V", "Lbk/soufit/theme/Heart;"))
CLASS_ANCHORED_SOURCES.add("LX/0HCj;")

BRIGHTNESS = "Lbk/soufit/feed/Brightness;"
MODEL_SOURCES.append(("Landroid/view/Window;", "setAttributes",
                      "(Landroid/view/WindowManager$LayoutParams;)V",
                      "(Landroid/view/Window;Landroid/view/WindowManager$LayoutParams;)V", BRIGHTNESS))
WINDOW_LABEL = "Window->setAttributes"

PLAYER_CONFIG = "Lcom/ss/android/ugc/aweme/video/simplayer/PlayerConfigImpl;"
HDR_LABEL = "native hdr switch"
HDR_QUESTION = "%s->flat()Z" % BRIGHTNESS

AVATAR_SHAPE = "Lbk/soufit/theme/AvatarShape;"
ROUND_DRAWABLES = ("LX/1Fnt;", "LX/0HEe;", "LX/0GvU;")
for anchor in ROUND_DRAWABLES:
    ANCHORED_SOURCES.append((anchor, "Landroid/graphics/Path;", "addCircle",
        "(FFFLandroid/graphics/Path$Direction;)V",
        "(Landroid/graphics/Path;FFFLandroid/graphics/Path$Direction;)V", AVATAR_SHAPE))
    CLASS_ANCHORED_SOURCES.add(anchor)

STORY_RING = "LX/11Wv;"
ANCHORED_SOURCES.append((STORY_RING, "Landroid/graphics/Canvas;", "drawArc",
    "(Landroid/graphics/RectF;FFZLandroid/graphics/Paint;)V",
    "(Landroid/graphics/Canvas;Landroid/graphics/RectF;FFZLandroid/graphics/Paint;)V", AVATAR_SHAPE))
CLASS_ANCHORED_SOURCES.add(STORY_RING)
FEED_RING = "LX/11Ww;"
STORY_RINGS = (STORY_RING, FEED_RING)
ANCHORED_SOURCES.append((FEED_RING, "Landroid/graphics/Canvas;", "drawArc",
    "(Landroid/graphics/RectF;FFZLandroid/graphics/Paint;)V",
    "(Landroid/graphics/Canvas;Landroid/graphics/RectF;FFZLandroid/graphics/Paint;)V", AVATAR_SHAPE))
CLASS_ANCHORED_SOURCES.add(FEED_RING)
ANCHORED_SOURCES.append(("Lcom/ss/android/ugc/aweme/commentv2/commentlist/powercell/BaseCommentCell;",
    VIEW,"findViewById","(I)Landroid/view/View;","(Landroid/view/View;I)Landroid/view/View;",COMMENTS))
for method in ("setIconRes", "setTintColor"):
    ANCHORED_SOURCES.append(("LX/0HCj;", "Lcom/bytedance/tux/icon/TuxIconView;", method,
        "(I)V", "(Lcom/bytedance/tux/icon/TuxIconView;I)V", "Lbk/soufit/theme/Heart;"))

DATE_CAPTIONS = (
    "Lcom/ss/android/ugc/aweme/feed/assem/desc/VideoDescVM;",
    "Lcom/ss/android/ugc/aweme/feed/assem/desc/VideoDescAssem;",
    "Lcom/ss/android/ugc/aweme/feed/assem/desc/LandscapeVideoDescVM;",
    "Lcom/ss/android/ugc/aweme/search/arch/v2/protocol/card/components/mediaplayer/components/desc/ui/SearchMediaTextDescAssem;",
)
for anchor in DATE_CAPTIONS:
    ANCHORED_SOURCES.append((anchor, AWEME, "getDesc", "()Ljava/lang/String;",
                             "(Ljava/lang/Object;)Ljava/lang/String;", DATES))
    CLASS_ANCHORED_SOURCES.add(anchor)

MODEL_SOURCES += [("Lcom/ss/android/ugc/aweme/im/streak/impl/StreakService;",name,original,replacement,target)
    for owner,name,original,replacement,target in list(MODEL_SOURCES)
    if owner==STREAK_SERVICE and isinstance(name,tuple) and name[0] in ("LJIJJLI","LJJJI")]
MODEL_SOURCES.append(("Landroid/app/Dialog;","show","()V","(Landroid/app/Dialog;)V","Lbk/soufit/media/DownloadChoice;"))

MODEL_SOURCES.append((VIEW,"setVisibility","(I)V","(Landroid/view/View;I)V",RAIL))

MODEL_STATICS += [
    (SUBSCRIPTIONS, "getDefaultDataSubscriptionId", "()I", "()I", REGION),
    (SUBSCRIPTIONS, "getDefaultVoiceSubscriptionId", "()I", "()I", REGION),
    (SUBSCRIPTIONS, "getDefaultSmsSubscriptionId", "()I", "()I", REGION),
    (SUBSCRIPTIONS, "getActiveDataSubscriptionId", "()I", "()I", REGION),
]

FIELD_SOURCES: List[Tuple[str, str, str, str, str]] = [
    (FEED_ITEM_LIST, "items", LIST, "rawItems", FEED),
    ("Lcom/ss/android/ugc/aweme/search/pages/result/topsearch/core/model/SearchMixFeedList;", "mItems", LIST, "searchItems", FEED),
    (VIDEO_CONTROL, "allowDownload", BOXED_BOOLEAN, "allowDownload", DOWNLOAD),
    (STICKER_ITEM, "stickerBase", STICKER_BASE, "stickerBase", STREAKS),
    (PHOTO_IMAGE, "ownerWatermarkImage", URL_MODEL, "ownerWatermarkImage", DOWNLOAD),
    (PHOTO_IMAGE, "userWatermarkImage", URL_MODEL, "userWatermarkImage", DOWNLOAD),
]

TIKTOK_PINK = 0xFFFE2C55

COLOUR_SOURCES: List[Tuple[str, str, str]] = [
    ("Landroid/content/res/Resources;", "getColor",
     "(I)I", "(Landroid/content/res/Resources;I)I"),
    ("Landroid/content/res/Resources;", "getColor",
     "(ILandroid/content/res/Resources$Theme;)I",
     "(Landroid/content/res/Resources;ILandroid/content/res/Resources$Theme;)I"),
    ("Landroid/content/res/TypedArray;", "getColor",
     "(II)I", "(Landroid/content/res/TypedArray;II)I"),
    ("Landroid/content/Context;", "getColor",
     "(I)I", "(Landroid/content/Context;I)I"),

    ("Landroid/content/res/TypedArray;", "getDrawable",
     "(I)Landroid/graphics/drawable/Drawable;",
     "(Landroid/content/res/TypedArray;I)Landroid/graphics/drawable/Drawable;"),
    ("Landroid/view/View;", "setBackgroundResource",
     "(I)V", "(Landroid/view/View;I)V"),

    ("Landroid/content/Context;", "getDrawable",
     "(I)Landroid/graphics/drawable/Drawable;",
     "(Landroid/content/Context;I)Landroid/graphics/drawable/Drawable;"),
    ("Landroid/content/res/Resources;", "getDrawable",
     "(I)Landroid/graphics/drawable/Drawable;",
     "(Landroid/content/res/Resources;I)Landroid/graphics/drawable/Drawable;"),
    ("Landroid/content/res/Resources;", "getDrawable",
     "(ILandroid/content/res/Resources$Theme;)Landroid/graphics/drawable/Drawable;",
     "(Landroid/content/res/Resources;ILandroid/content/res/Resources$Theme;)"
     "Landroid/graphics/drawable/Drawable;"),
    ("Landroid/widget/ImageView;", "setImageResource",
     "(I)V", "(Landroid/widget/ImageView;I)V"),
    ("Landroid/widget/ImageView;", "setImageTintList",
     "(Landroid/content/res/ColorStateList;)V",
     "(Landroid/widget/ImageView;Landroid/content/res/ColorStateList;)V"),

    ("Landroid/content/res/Resources;", "openRawResource",
     "(I)Ljava/io/InputStream;",
     "(Landroid/content/res/Resources;I)Ljava/io/InputStream;"),
    ("Landroid/content/res/Resources;", "openRawResource",
     "(ILandroid/util/TypedValue;)Ljava/io/InputStream;",
     "(Landroid/content/res/Resources;ILandroid/util/TypedValue;)Ljava/io/InputStream;"),
    ("Landroid/content/res/AssetManager;", "open",
     "(Ljava/lang/String;)Ljava/io/InputStream;",
     "(Landroid/content/res/AssetManager;Ljava/lang/String;)Ljava/io/InputStream;"),
    ("Landroid/content/res/AssetManager;", "open",
     "(Ljava/lang/String;I)Ljava/io/InputStream;",
     "(Landroid/content/res/AssetManager;Ljava/lang/String;I)Ljava/io/InputStream;"),

    ("Landroid/view/View;", "setAlpha", "(F)V", "(Landroid/view/View;F)V"),

    ("Landroid/content/res/Resources;", "getColorStateList",
     "(I)Landroid/content/res/ColorStateList;",
     "(Landroid/content/res/Resources;I)Landroid/content/res/ColorStateList;"),
    ("Landroid/content/res/TypedArray;", "getColorStateList",
     "(I)Landroid/content/res/ColorStateList;",
     "(Landroid/content/res/TypedArray;I)Landroid/content/res/ColorStateList;"),

    ("Landroid/view/Window;", "setStatusBarColor",
     "(I)V", "(Landroid/view/Window;I)V"),
    ("Landroid/view/Window;", "setNavigationBarColor",
     "(I)V", "(Landroid/view/Window;I)V"),
    ("Landroid/widget/TextView;", "setHintTextColor",
     "(I)V", "(Landroid/widget/TextView;I)V"),
    ("Landroid/graphics/drawable/Drawable;", "setTint",
     "(I)V", "(Landroid/graphics/drawable/Drawable;I)V"),
    ("Landroid/graphics/drawable/GradientDrawable;", "setColors",
     "([I)V", "(Landroid/graphics/drawable/GradientDrawable;[I)V"),
    ("Landroid/graphics/Paint;", "setColor",
     "(I)V", "(Landroid/graphics/Paint;I)V"),
    ("Landroid/text/TextPaint;", "setColor",
     "(I)V", "(Landroid/graphics/Paint;I)V"),
    ("Landroid/graphics/drawable/GradientDrawable;", "setColor",
     "(I)V", "(Landroid/graphics/drawable/GradientDrawable;I)V"),
    ("Landroid/widget/ImageView;", "setColorFilter",
     "(I)V", "(Landroid/widget/ImageView;I)V"),
    ("Landroid/widget/ImageView;", "setColorFilter",
     "(ILandroid/graphics/PorterDuff$Mode;)V",
     "(Landroid/widget/ImageView;ILandroid/graphics/PorterDuff$Mode;)V"),
    ("Landroid/widget/TextView;", "setTextColor",
     "(I)V", "(Landroid/widget/TextView;I)V"),
    ("Lcom/bytedance/tux/input/TuxTextView;", "setTextColor",
     "(I)V", "(Landroid/widget/TextView;I)V"),
    ("Lcom/bytedance/tux/input/TuxTextView;", "setHintTextColor",
     "(I)V", "(Landroid/widget/TextView;I)V"),
    ("Landroid/view/View;", "setBackgroundColor",
     "(I)V", "(Landroid/view/View;I)V"),
    ("Lcom/bytedance/tux/input/TuxTextView;", "setTextColorRes", "(I)V", "(Landroid/widget/TextView;I)V"),
    ("Lcom/bytedance/tux/input/TuxTextView;", "setTextColor", "(I)V", "(Landroid/widget/TextView;I)V"),
    ("Lcom/bytedance/tux/input/TuxTextView;", "setTextColor", "(Landroid/content/res/ColorStateList;)V", "(Landroid/widget/TextView;Landroid/content/res/ColorStateList;)V"),
    ("Lcom/bytedance/tux/icon/TuxIconView;", "setTintColorRes", "(I)V", "(Lcom/bytedance/tux/icon/TuxIconView;I)V"),
    ("Lcom/bytedance/tux/icon/TuxIconView;", "setTintColor", "(I)V", "(Lcom/bytedance/tux/icon/TuxIconView;I)V"),
    ("Lcom/bytedance/tux/icon/TuxIconView;", "setColor",
     "(I)V", "(Lcom/bytedance/tux/icon/TuxIconView;I)V"),
]

for _owner, _name, _descriptor, _receiver in (
    ("Landroid/graphics/drawable/Drawable;", "setTintList", "(Landroid/content/res/ColorStateList;)V", "Landroid/graphics/drawable/Drawable;"),
    ("Landroid/view/View;", "setBackgroundTintList", "(Landroid/content/res/ColorStateList;)V", "Landroid/view/View;"),
    ("Landroid/graphics/drawable/GradientDrawable;", "setColor", "(Landroid/content/res/ColorStateList;)V", "Landroid/graphics/drawable/GradientDrawable;"),
    ("Landroid/graphics/drawable/GradientDrawable;", "setStroke", "(II)V", "Landroid/graphics/drawable/GradientDrawable;"),
    ("Landroid/graphics/drawable/GradientDrawable;", "setStroke", "(ILandroid/content/res/ColorStateList;)V", "Landroid/graphics/drawable/GradientDrawable;"),
    ("Landroid/widget/TextView;", "setTextColor", "(Landroid/content/res/ColorStateList;)V", "Landroid/widget/TextView;"),
    ("Landroid/widget/TextView;", "setHintTextColor", "(Landroid/content/res/ColorStateList;)V", "Landroid/widget/TextView;"),
    ("Lcom/bytedance/tux/input/TuxTextView;", "setTextColor", "(Landroid/content/res/ColorStateList;)V", "Landroid/widget/TextView;"),
    ("Lcom/bytedance/tux/input/TuxTextView;", "setHintTextColor", "(Landroid/content/res/ColorStateList;)V", "Landroid/widget/TextView;"),
):
    COLOUR_SOURCES.append((_owner,_name,_descriptor,"("+_receiver+_descriptor[1:]))

COLOUR_STATICS: List[Tuple[str, str, str]] = [
    ("Landroid/content/res/ColorStateList;", "valueOf",
     "(I)Landroid/content/res/ColorStateList;"),
]

TARGETS: List[Tuple[str, str, str]] = [
    ("getSimCountryIso", "()Ljava/lang/String;", "(%s)Ljava/lang/String;" % TELEPHONY),
    ("getNetworkCountryIso", "()Ljava/lang/String;", "(%s)Ljava/lang/String;" % TELEPHONY),
    ("getSimOperator", "()Ljava/lang/String;", "(%s)Ljava/lang/String;" % TELEPHONY),
    ("getNetworkOperator", "()Ljava/lang/String;", "(%s)Ljava/lang/String;" % TELEPHONY),
    ("getSimOperatorName", "()Ljava/lang/String;", "(%s)Ljava/lang/String;" % TELEPHONY),
    ("getNetworkOperatorName", "()Ljava/lang/String;", "(%s)Ljava/lang/String;" % TELEPHONY),
    ("getSimState", "()I", "(%s)I" % TELEPHONY),
    ("getSimState", "(I)I", "(%sI)I" % TELEPHONY),
    ("hasIccCard", "()Z", "(%s)Z" % TELEPHONY),
    ("isNetworkRoaming", "()Z", "(%s)Z" % TELEPHONY),
    ("getSimCarrierId", "()I", "(%s)I" % TELEPHONY),
]


FORCED_FALSE: List[Tuple[str, str]] = [
    ("com/bytedance/lobby/google/GoogleAuth", "isAvailable()Z"),
]


def rules() -> List[Tuple[str, "re.Pattern[str]", str]]:
    out = []
    for name, original, replacement in TARGETS:
        pattern = re.compile(
            r"invoke-virtual(/range)? (\{[^}]*\}), %s->%s%s"
            % (re.escape(TELEPHONY), name, re.escape(original))
        )
        target = r"invoke-static\1 \2, %s->%s%s" % (REGION, name, replacement)
        out.append((name + original, pattern, target))
    return out


def accent_rules() -> List[Tuple[str, "re.Pattern[str]", str]]:
    out = []
    literal = "-0x%x" % ((1 << 32) - TIKTOK_PINK) if TIKTOK_PINK > 0x7FFFFFFF \
        else "0x%x" % TIKTOK_PINK
    out.append((
        "the pink itself",
        re.compile(r"^(\s*)const ([vp]\d+), %s$" % re.escape(literal), re.MULTILINE),
        r"\1invoke-static {}, %s->accent()I\n\n\1move-result \2" % ACCENT,
    ))
    for owner, name, original, replacement in COLOUR_SOURCES:
        out.append((
            "%s->%s%s" % (owner.split("/")[-1][:-1], name, original),
            re.compile(r"invoke-virtual(/range)? (\{[^}]*\}), %s->%s%s"
                       % (re.escape(owner), name, re.escape(original))),
            r"invoke-static\1 \2, %s->%s%s" % (ACCENT, name, replacement),
        ))
    for owner, name, signature in COLOUR_STATICS:
        out.append((
            "%s->%s" % (owner.split("/")[-1][:-1], name),
            re.compile(r"invoke-static(/range)? (\{[^}]*\}), %s->%s%s"
                       % (re.escape(owner), name, re.escape(signature))),
            r"invoke-static\1 \2, %s->%s%s" % (ACCENT, name, signature),
        ))
    out.append((
        "feed Lottie setAnimation",
        re.compile(r"invoke-virtual(/range)? (\{[^}]*\}), "
                   r"LX/1HkT;->setAnimation\(Ljava/lang/String;\)V"),
        r"invoke-static\1 \2, %s->setAnimation(Ljava/lang/Object;Ljava/lang/String;)V"
        % ACCENT,
    ))
    return out


for _font_owner in (TEXT_VIEW,TUX_TEXT,"Landroid/widget/EditText;","Landroid/widget/Button;"):
    for _font_name,_font_sig in (
        ("setText","(I)V"),("setHint","(I)V"),("setHint","(Ljava/lang/CharSequence;)V"),
        ("setTextAppearance","(I)V"),("setTextAppearance","(Landroid/content/Context;I)V"),
    ):
        MODEL_SOURCES.append((_font_owner,_font_name,_font_sig,"("+TEXT_VIEW+_font_sig[1:],FONTS))


MODEL_SOURCES += [
    (FRIENDS_RESPONSE,"getUserList","()Ljava/util/List;","(Ljava/lang/Object;)Ljava/util/List;","Lbk/soufit/social/PinnedFriends;"),
    ("Landroid/app/NotificationManager;","notify","(ILandroid/app/Notification;)V","(Landroid/app/NotificationManager;ILandroid/app/Notification;)V","Lbk/soufit/social/ModNotifications;"),
    ("Landroid/app/NotificationManager;","notify","(Ljava/lang/String;ILandroid/app/Notification;)V","(Landroid/app/NotificationManager;Ljava/lang/String;ILandroid/app/Notification;)V","Lbk/soufit/social/ModNotifications;"),
]

def model_rules() -> List[Tuple[str, "re.Pattern[str]", str]]:
    out = []
    for owner, name, original, replacement, target in MODEL_SOURCES:
        theirs, ours = name if isinstance(name, tuple) else (name, name)
        out.append((
            "%s->%s" % (owner.rsplit("/", 1)[-1][:-1], theirs),
            re.compile(r"invoke-(?:virtual|interface)(/range)? (\{[^}]*\}), %s->%s%s"
                       % (re.escape(owner), theirs, re.escape(original))),
            r"invoke-static\1 \2, %s->%s%s" % (target, ours, replacement),
        ))
    for owner, name, original, replacement, target in MODEL_STATICS:
        theirs, ours = name if isinstance(name, tuple) else (name, name)
        out.append((
            "%s->%s" % (owner.rsplit("/", 1)[-1][:-1], theirs),
            re.compile(r"invoke-static(/range)? (\{[^}]*\}), %s->%s%s"
                       % (re.escape(owner), theirs, re.escape(original))),
            r"invoke-static\1 \2, %s->%s%s" % (target, ours, replacement),
        ))
    for label, descriptor, ours, target in DISCOVERED_VIRTUALS:
        found = FOUND.get(label)
        if found is None:
            continue
        owner, theirs = found
        out.append((
            label,
            re.compile(r"invoke-virtual(/range)? (\{[^}]*\}), %s->%s%s"
                       % (re.escape(owner), re.escape(theirs), re.escape(descriptor))),
            r"invoke-static\1 \2, %s->%s%s"
            % (target, ours, "(Ljava/lang/Object;" + descriptor[1:]),
        ))
    for label, descriptor, ours, target in DISCOVERED_STATICS:
        found = FOUND.get(label)
        if found is None:
            continue
        owner, theirs = found
        out.append((
            label,
            re.compile(r"invoke-static(/range)? (\{[^}]*\}), %s->%s%s"
                       % (re.escape(owner), re.escape(theirs), re.escape(descriptor))),
            r"invoke-static\1 \2, %s->%s%s" % (target, ours, descriptor),
        ))
    for name, original, replacement, target in WILD_SOURCES:
        out.append((
            "%s (any owner)" % name,
            re.compile(r"invoke-(?:virtual|interface)(/range)? (\{[^}]*\}), L[^;]+;->%s%s"
                       % (name, re.escape(original))),
            r"invoke-static\1 \2, %s->%s%s" % (target, name, replacement),
        ))
    for owner, field, kind, name, target in FIELD_SOURCES:
        if kind in ("J", "D"):
            read, took = "iget-wide", "move-result-wide"
        elif kind in ("Z", "B", "S", "C", "I", "F"):
            read, took = "iget", "move-result"
        else:
            read, took = "iget-object", "move-result-object"
        out.append((
            "%s.%s" % (owner.rsplit("/", 1)[-1][:-1], field),
            re.compile(r"^(\s*)%s ([vp]\d+), ([vp]\d+), %s->%s:%s$"
                       % (read, re.escape(owner), re.escape(field), re.escape(kind)),
                       re.MULTILINE),
            r"\1invoke-static {\3}, %s->%s(%s)%s\n\n\1%s \2"
            % (target, name, owner, kind, took),
        ))
    return out


def rewrite_models(root: str) -> Dict[str, int]:
    counts: Dict[str, int] = {}
    prepared = model_rules()
    needles = [_required(pattern) for _label, pattern, _target in prepared]
    owners = tuple(set([owner for owner, _n, _o, _r, _t in MODEL_SOURCES]
                       + [owner for owner, _n, _o, _r, _t in MODEL_STATICS]
                       + [owner for owner, _f, _k, _n, _t in FIELD_SOURCES]
                       + [name for name, _o, _r, _t in WILD_SOURCES]
                       + [FOUND[label][0]
                          for label, _d, _o, _t in DISCOVERED_STATICS + DISCOVERED_VIRTUALS
                          if label in FOUND]))
    for dirpath, _dirs, files in os.walk(root):
        for name in files:
            if not name.endswith(".smali"):
                continue
            path = os.path.join(dirpath, name)
            with open(path, encoding="utf-8") as handle:
                text = handle.read()
            if not any(owner in text for owner in owners):
                continue
            before = text
            for (label, pattern, target), needle in zip(prepared, needles):
                if needle and needle not in text:
                    continue
                text, hits = pattern.subn(target, text)
                if hits:
                    counts[label] = counts.get(label, 0) + hits
            if text != before:
                with open(path, "w", encoding="utf-8") as handle:
                    handle.write(text)
    return counts


ANCHORED_SOURCES.append(("LX/0oir;","LX/0oir;","LIZ",
    "(Ljava/lang/String;Ljava/lang/String;Ljava/util/List;)Landroid/content/ClipData;",
    "(Ljava/lang/String;Ljava/lang/String;Ljava/util/List;)Landroid/content/ClipData;",
    "Lbk/soufit/feed/CommentClipboard;"))

def anchored_rules() -> List[Tuple[str, str, "re.Pattern[str]", str]]:
    out = []
    for anchor, owner, name, original, replacement, target in ANCHORED_SOURCES:
        out.append((
            anchor,
            "%s->%s (in the class marked %s)" % (owner.rsplit("/", 1)[-1][:-1], name, anchor),
            re.compile(r"invoke-%s(/range)? (\{[^}]*\}), %s->%s%s"
                       % ("static" if owner=="LX/0oir;" else "virtual", re.escape(owner), name, re.escape(original))),
            r"invoke-static\1 \2, %s->%s%s" % (target, name, replacement),
        ))
    return out


def rewrite_anchored(root: str) -> Dict[str, int]:
    counts: Dict[str, int] = {}
    prepared = anchored_rules()
    for dirpath, _dirs, files in os.walk(root):
        for name in files:
            if not name.endswith(".smali"):
                continue
            path = os.path.join(dirpath, name)
            with open(path, encoding="utf-8") as handle:
                text = handle.read()
            before = text
            declaration=text.split("\n",1)[0]
            if FEED_ITEM_LIST in declaration:
                def native_ad_getter(match):
                    body=match.group(0)
                    if "Feed;->withoutAds" in body:return body
                    body,hits=re.subn(r"(?m)^    return-object ([vp]\d+)",lambda m:"    invoke-static/range {"+m.group(1)+" .. "+m.group(1)+"}, "+FEED+"->withoutAds(Ljava/util/List;)Ljava/util/List;\n    move-result-object "+m.group(1)+"\n"+m.group(0),body)
                    if hits:counts["native feed ad getter"]=hits
                    return body
                text=re.sub(r"(?ms)^\.method public getItems\(\)Ljava/util/List;.*?^\.end method",native_ad_getter,text)
                def native_ad_setter(match):
                    body=match.group(0)
                    if "Feed;->withoutAds" in body:return body
                    hook="\n    invoke-static/range {p1 .. p1}, "+FEED+"->withoutAds(Ljava/util/List;)Ljava/util/List;\n    move-result-object p1\n"
                    body,hits=re.subn(r"(?m)^    \.registers [0-9]+",lambda m:m.group(0)+hook,body,count=1)
                    if hits:counts["native feed ad ingress"]=hits
                    return body
                text=re.sub(r"(?ms)^\.method public setItems\(Ljava/util/List;\)V.*?^\.end method",native_ad_setter,text)
            if "comment" in declaration.lower() and ("Fragment" in declaration or "Panel" in declaration or "Sheet" in declaration):
                add_exit(
                    r"(?ms)^\.method public (?:final )?onViewCreated\(Landroid/view/View;\)V.*?^\.end method",
                    "    invoke-static/range {p1 .. p1}, " + COMMENTS + "->hideFeelingsBar(Landroid/view/View;)V\n",
                    "native feelings bar hide"
                )
            if "Lcom/ss/android/ugc/aweme/comment/longpresspanel/ui/CommentLongPressFragment;" in declaration:
                def native_comment_items(match):
                    body=match.group(0)
                    if "Comments;->nativeItems" in body:return body
                    hook="\n    invoke-static {p0, p1}, "+COMMENTS+"->nativeItems(Ljava/lang/Object;Ljava/util/List;)Ljava/util/List;\n    move-result-object p1\n"
                    body,hits=re.subn(r"(?m)^    \.registers [0-9]+",lambda m:m.group(0)+hook,body,count=1)
                    if hits:counts["native comment selection item"]=hits
                    return body
                text=re.sub(r"(?ms)^\.method public final oX\(Ljava/util/List;\)V.*?^\.end method",native_comment_items,text)
            if "Lcom/ss/android/ugc/aweme/offlinemode/ui/sheet/OfflineModeSheetPageAssem;" in declaration:
                def offline_legacy(match):
                    body=match.group(0)
                    hook="    invoke-static/range {p0 .. p0}, Lbk/soufit/feed/OfflineChoices;->legacy(Ljava/lang/Object;)V\n"
                    if hook.strip() not in body:
                        body=re.sub(r"(?m)^    return-void",hook+"    return-void",body)
                        counts["native offline legacy menu"]=counts.get("native offline legacy menu",0)+1
                    return body
                text=re.sub(r"(?ms)^\.method public final onAssemPostCreate\(\)V.*?^\.end method",offline_legacy,text)
            comment_owner = next((owner for owner in (
                "Lcom/ss/android/ugc/aweme/comment/commentlist/api/CommentApi;",
                "LX/0oah;",
                "Lcom/ss/android/ugc/aweme/commentv2/commentlist/CommentApiV2;",
                "Lcom/ss/android/ugc/aweme/commentv2/commentlist/CommentApiV2SplitJson;"
            ) if owner in declaration), None)
            if comment_owner:
                def offline_native_response(match):
                    body=match.group(0)
                    if "OfflineComments;->native" in body:return body
                    task=comment_owner.endswith("/CommentApi;")
                    static=task or comment_owner=="LX/0oah;"
                    register="p0" if static else "p1"
                    method="nativeTask" if task else "nativeObservable"
                    result="LX/0Vgo;" if task else "LX/19ZK;"
                    hook=(f"    invoke-static/range {{{register} .. {register}}}, Lbk/soufit/feed/OfflineComments;->{method}(Ljava/lang/String;)Ljava/lang/Object;\n"
                          "    move-result-object v0\n    if-eqz v0, :soufit_comments_online\n"
                          f"    check-cast v0, {result}\n    return-object v0\n    :soufit_comments_online\n")
                    body,hits=re.subn(r"(?m)^(    (?:move|const|invoke|sget|iget|new|return|if|check)[\w/-]*(?: |$))",lambda m:hook+m.group(0),body,count=1)
                    if hits:counts["native offline comments response"]=counts.get("native offline comments response",0)+1
                    return body
                signature=r"(?ms)^\.method public static LIZIZ\(.*?^\.end method" if comment_owner in ("LX/0oah;","Lcom/ss/android/ugc/aweme/comment/commentlist/api/CommentApi;") else r"(?ms)^\.method public fetchCommentListV2\(.*?^\.end method"
                text=re.sub(signature,offline_native_response,text)
            if "LX/0wk3;" in declaration:
                def offline_saved(match):
                    body=match.group(0)
                    if "OfflineComments;->cached" not in body:
                        body=re.sub(r"(?m)^    \.registers \d+",lambda m:m.group(0)+"\n    invoke-static/range {p0 .. p1}, Lbk/soufit/feed/OfflineComments;->cached(Ljava/lang/Object;Ljava/lang/String;)V",body,count=1)
                        counts["native offline comments cache"]=counts.get("native offline comments cache",0)+1
                    return body
                text=re.sub(r"(?ms)^\.method public static LIZ\(Lcom/ss/android/ugc/aweme/feed/model/Aweme;Ljava/lang/String;\)V.*?^\.end method",offline_saved,text)
            if "Lcom/ss/android/ugc/aweme/offlinemode/ui/OfflineModeContentComponent;" in declaration:
                def offline_content(match):
                    body=match.group(0)
                    if "OfflineComments;->content" not in body:
                        body=re.sub(r"(?m)^    \.registers \d+",lambda m:m.group(0)+"\n    invoke-static/range {p0 .. p0}, Lbk/soufit/feed/OfflineComments;->content(Ljava/lang/Object;)V",body,count=1)
                        counts["native offline comments current video"]=counts.get("native offline comments current video",0)+1
                    return body
                text=re.sub(r"(?ms)^\.method public final onParentViewCreated\(\)V.*?^\.end method",offline_content,text)
                def offline_closed(match):
                    body=match.group(0)
                    if "OfflineComments;->closed" not in body:
                        body=re.sub(r"(?m)^    return-void","    invoke-static/range {p0 .. p0}, Lbk/soufit/feed/OfflineComments;->closed(Ljava/lang/Object;)V\n    return-void",body)
                    return body
                text=re.sub(r"(?ms)^\.method public final onDestroy\(\)V.*?^\.end method",offline_closed,text)
            if "Lcom/ss/android/ugc/aweme/offlinemode/ui/bottom/OfflineModeDetailBottomComponent;" in declaration:
                def offline_bottom(match):
                    body=match.group(0)
                    if "OfflineComments;->bottom" not in body and "    return-object v4" in body:
                        body=body.replace("    return-object v4","    invoke-static {v4}, Lbk/soufit/feed/OfflineComments;->bottom(Landroid/view/View;)Landroid/view/View;\n    move-result-object v4\n    return-object v4")
                        counts["native offline comments viewer"]=counts.get("native offline comments viewer",0)+1
                    return body
                text=re.sub(r"(?ms)^\.method public final MR2\(Landroid/view/ViewGroup;\)Landroid/view/View;.*?^\.end method",offline_bottom,text)
            if "Lcom/ss/android/ugc/aweme/offlinemode/viewmodel/OfflineModeManagerVM;" in declaration:
                def offline_valid(match):
                    body=match.group(0)
                    needle="invoke-interface {v1, v0}, Ljava/util/List;->contains(Ljava/lang/Object;)Z"
                    if needle in body:
                        body=body.replace(needle,"invoke-static {v1, v0}, Lbk/soufit/feed/OfflineChoices;->validCount(Ljava/util/List;Ljava/lang/Object;)Z")
                        counts["native offline custom persistence"]=counts.get("native offline custom persistence",0)+1
                    return body
                text=re.sub(r"(?ms)^\.method public final d43\(\)Z.*?^\.end method",offline_valid,text)
            if "LX/0wjq;" in declaration:
                def offline_native_count(match):
                    body=match.group(0)
                    needle="invoke-interface {v1, v0}, Ljava/util/List;->contains(Ljava/lang/Object;)Z"
                    if needle in body:
                        body=body.replace(needle,"invoke-static {v1, v0}, Lbk/soufit/feed/OfflineChoices;->validCount(Ljava/util/List;Ljava/lang/Object;)Z",1)
                        counts["native offline count accepted"]=counts.get("native offline count accepted",0)+1
                    return body
                text=re.sub(r"(?ms)^\.method public static LIZ\(\)I.*?^\.end method",offline_native_count,text)
            if "Lkotlin/jvm/internal/AwS704S0100000_26;" in declaration:
                def offline_list(match):
                    body=match.group(0)
                    hook="\n    invoke-static {v1}, Lbk/soufit/feed/OfflineChoices;->counts(Ljava/util/List;)Ljava/util/List;\n    move-result-object v1"
                    needle=re.search(r"invoke-static \{\}, LX/0wj1;->LJFF\(\)Ljava/util/List;(?:(?:\s*\.line \d+)*)\s*move-result-object v1",body)
                    if "OfflineChoices;->counts" not in body and needle:
                        body=body[:needle.end()]+hook+body[needle.end():]
                        counts["native offline choices"]=counts.get("native offline choices",0)+1
                    return body
                text=re.sub(r"(?ms)^\.method public static final invoke\$3208\(Lkotlin/jvm/internal/AwS704S0100000_26;\)Ljava/lang/Object;.*?^\.end method",offline_list,text)
            if "LX/0wiL;" in declaration:
                def offline_row(match):
                    body=match.group(0)
                    if "OfflineChoices;->row" not in body:
                        body=body.replace("    return-object v10","    invoke-static {v3, v10}, Lbk/soufit/feed/OfflineChoices;->row(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;\n    move-result-object v10\n    check-cast v10, LX/0ldB;\n    return-object v10")
                        counts["native offline row"]=counts.get("native offline row",0)+1
                    return body
                text=re.sub(r"(?ms)^\.method public final LIZLLL\(\)LX/0ldB;.*?^\.end method",offline_row,text)
            def add_exit(pattern,hook,label):
                nonlocal text
                def patch(match):
                    body=match.group(0)
                    if hook.strip() in body:return body
                    changed,hits=re.subn(r"(?m)^([ \t]*)return-void",lambda m:hook+m.group(0),body)
                    if hits:counts[label]=counts.get(label,0)+1
                    return changed
                text=re.sub(pattern,patch,text)
            if "Lcom/ss/android/ugc/aweme/feed/assem/share/VideoShareAssem;" in declaration:
                def capture_share(match):
                    body=match.group(0)
                    if "VideoActions;->captureShare" in body:return body
                    body=re.sub(r"(?m)^    \.registers \d+",lambda m:m.group(0)+"\n    invoke-static/range {p0 .. p0}, Lbk/soufit/feed/VideoActions;->captureShare(Ljava/lang/Object;)V",body,count=1)
                    counts["native share capture"]=counts.get("native share capture",0)+1
                    return body
                text=re.sub(r"(?ms)^\.method public final onClick\(Landroid/view/View;\)V.*?^\.end method",capture_share,text)
            if REPOST_CLASS in declaration:
                add_exit(r"(?ms)^\.method public final (?:onViewCreated\(Landroid/view/View;\)|s4\(Ljava/lang/Object;\))V.*?^\.end method","    invoke-static/range {p0 .. p0}, Lbk/soufit/feed/Repost;->bind(Ljava/lang/Object;)V\n","native repost")
            if AVATAR_CLASS in declaration:
                add_exit(r"(?ms)^\.method public final (?:onViewCreated\(Landroid/view/View;\)|s4\(Ljava/lang/Object;\))V.*?^\.end method","    invoke-static/range {p0 .. p0}, Lbk/soufit/feed/Repost;->anchor(Ljava/lang/Object;)V\n","native repost avatar")
            if PROFILE_CONTAINER in declaration:
                add_exit(r"(?ms)^\.method public onViewCreated\(Landroid/view/View;\)V.*?^\.end method","    invoke-static/range {p0 .. p1}, Lbk/soufit/profile/ProfileLayout;->bind(Ljava/lang/Object;Landroid/view/View;)V\n","native profile layout")
            if any(owner in declaration for owner in MESSAGE_CLASSES):
                def message_bind(match):
                    body=match.group(0)
                    if "MessageTimes;->bind" in body:return body
                    body=re.sub(r"(?m)^    \.registers \d+",lambda m:m.group(0)+"\n    invoke-static/range {p0 .. p0}, Lbk/soufit/social/MessageTimes;->before(Ljava/lang/Object;)V",body,count=1)
                    body=re.sub(r"(?m)^    return-void","    invoke-static/range {p0 .. p1}, Lbk/soufit/social/MessageTimes;->bind(Ljava/lang/Object;Ljava/lang/Object;)V\n    return-void",body)
                    counts["native message timestamps"]=counts.get("native message timestamps",0)+1
                    return body
                message_pattern=(r"(?ms)^\.method public Bq\(LX/0Qfl;\)V.*?^\.end method" if MESSAGE_CLASSES[0] in declaration else r"(?ms)^\.method public final s4\(Ljava/lang/Object;\)V.*?^\.end method")
                text=re.sub(message_pattern,message_bind,text)
            if PUSH_GUIDE in declaration:
                def push_guide(match):
                    body=match.group(0)
                    if "ModNotifications;->noPrompts" in body:return body
                    hook="\n    invoke-static {}, Lbk/soufit/social/ModNotifications;->noPrompts()Z\n    move-result v0\n    if-eqz v0, :soufit_notification_guide\n    const/4 v0, 0x0\n    return v0\n    :soufit_notification_guide"
                    body=re.sub(r"(?m)^    \.registers \d+",lambda m:m.group(0)+hook,body,count=1)
                    counts["native notification guide"]=counts.get("native notification guide",0)+1
                    return body
                text=re.sub(r"(?ms)^\.method public final LIZ\(\)Z.*?^\.end method",push_guide,text)
            if PLAYER_CONTROLLER in declaration:
                def resumed_player(match):
                    body=match.group(0)
                    if "PlayerTools;->resumed" in body:return body
                    changed,hits=re.subn(r"(?m)^([ \t]*)return-void",lambda m:"    invoke-static/range {p0 .. p1}, Lbk/soufit/feed/PlayerTools;->resumed(Ljava/lang/Object;Ljava/lang/String;)V\n"+m.group(0),body)
                    if hits:counts["native player resume"]=counts.get("native player resume",0)+1
                    return changed
                text=re.sub(r"(?ms)^\.method public final onResumePlay\(Ljava/lang/String;\)V.*?^\.end method",resumed_player,text)
            if any(owner in declaration for owner in RAIL_CLASSES):
                def rail_slot(match):
                    body=match.group(0)
                    if "FeedRail;->bindNative" in body:return body
                    hook="    invoke-static/range {p0 .. p0}, "+RAIL+"->bindNative(Ljava/lang/Object;)V\n"
                    changed,hits=re.subn(r"(?m)^([ \t]*)return-void",lambda m:hook+m.group(0),body)
                    if hits:counts["native right-area slots"]=counts.get("native right-area slots",0)+1
                    return changed
                text=re.sub(r"(?ms)^\.method public final (?:onViewCreated\(Landroid/view/View;\)|s4\(Ljava/lang/Object;\))V.*?^\.end method",rail_slot,text)
            for anchor, label, pattern, target in prepared:
                if anchor not in text:
                    continue
                if anchor in CLASS_ANCHORED_SOURCES:
                    declaration = text.split("\n", 1)[0]
                    if anchor not in declaration:
                        continue
                text, hits = pattern.subn(target, text)
                if hits:
                    counts[label] = counts.get(label, 0) + hits
            if "LX/0dQx;" in text.split("\n",1)[0]:
                def repost_texture(match):
                    body=match.group(0)
                    if "Repost;->textureChanged" in body:return body
                    body,hits=re.subn(r"(?m)^([ \t]*return-void)","    invoke-static/range {p0 .. p0}, Lbk/soufit/feed/Repost;->textureChanged(Landroid/view/View;)V\n"+r"\1",body)
                    if hits:counts["native repost texture state"]=counts.get("native repost texture state",0)+hits
                    return body
                text=re.sub(r"(?ms)^\.method public final (?:LJ|LJFF)\(\)V.*?^\.end method",repost_texture,text)
            if "LX/0oYe;" in text.split("\n",1)[0]:
                def selected_dislike(match):
                    body=match.group(0)
                    if "Accent;->dislike" in body:return body
                    body,hits=re.subn(r"(?m)^([ \t]*return-void)","    invoke-static/range {p0 .. p0}, Lbk/soufit/theme/Accent;->dislike(Ljava/lang/Object;)V\n"+r"\1",body)
                    if hits:counts["selected dislike accent"]=counts.get("selected dislike accent",0)+hits
                    return body
                text=re.sub(r"(?ms)^\.method public final LJIIIZ\(Ljava/lang/Boolean;\)V.*?^\.end method",selected_dislike,text)
            if "Lcom/ss/android/ugc/aweme/commentv2/commentlist/powercell/BaseCommentCell;" in text.split("\n",1)[0]:
                def bind_comment_body(match):
                    body=match.group(0)
                    if "Comments;->bindCell" in body:return body
                    body,hits=re.subn(r"(?m)^([ \t]*return-void)","    invoke-static/range {p0 .. p0}, "+COMMENTS+"->bindCell(Ljava/lang/Object;)V\n"+r"\1",body)
                    if hits:counts["native comment body actions"]=counts.get("native comment body actions",0)+hits
                    return body
                text=re.sub(r"(?ms)^\.method public bridge synthetic onBindItemView\([^\n]*\n.*?^\.end method",bind_comment_body,text)
                def native_comment_actions(match):
                    method=match.group(0)
                    if "CommentActionMenuVM;->h43" not in method or "Comments;->openNativeActions" in method:return method
                    hook=("\n    invoke-static/range {p0 .. p0}, "+COMMENTS+"->openNativeActions(Ljava/lang/Object;)Z\n"
                          "    move-result v0\n    if-eqz v0, :soufit_native_comment_menu\n    return-void\n    :soufit_native_comment_menu\n")
                    result,hits=re.subn(r"(?m)^(    \.registers [0-9]+)",lambda m:m.group(0)+hook,method,count=1)
                    if hits:counts["native comment text actions"]=counts.get("native comment text actions",0)+hits
                    return result
                text=re.sub(r"(?ms)^\.method public k8\(\)V.*?^\.end method",native_comment_actions,text)
                pattern = (r"(?m)^([ \t]*iget-object ([vp]\d+), [vp]\d+, "
                    r"Landroidx/recyclerview/widget/RecyclerView\$ViewHolder;->itemView:Landroid/view/View;)"
                    r"(?!\n[ \t]*invoke-static/range \{\2 \.\. \2\}, Lbk/soufit/feed/Comments;->mark)")
                text,hits=re.subn(pattern,lambda m:m.group(1)+"\n    invoke-static/range {"+m.group(2)+" .. "+m.group(2)+"}, "
                    +COMMENTS+"->mark(Landroid/view/View;)V",text)
                if hits:counts["native comment cell protection"]=counts.get("native comment cell protection",0)+hits
            if "Lcom/ss/android/ugc/aweme/feed/assem/videoauthorinfo/VideoAuthorInfoRelationAssem;" in text.split("\n",1)[0]:
                def author_date(match):
                    method=match.group(0)
                    if "Dates;->bindAuthor" in method:return method
                    changed,hits=re.subn(r"(?m)^([ \t]*return-void)","    invoke-static/range {p0 .. p0}, "+DATES+"->bindAuthor(Ljava/lang/Object;)V\n"+r"\1",method)
                    if hits:counts["native author date slot"]=counts.get("native author date slot",0)+hits
                    return changed
                text=re.sub(r"(?ms)^\.method public final (?:onViewCreated\(Landroid/view/View;\)V|s4\(Ljava/lang/Object;\)V).*?^\.end method",author_date,text)
            if "Lcom/ss/android/ugc/aweme/feed/assem/desc/VideoDescAssem;" in text.split("\n",1)[0]:
                def selected_date(match):
                    method=match.group(0)
                    if "onHolderSelected: awemeId=" not in method or "Dates;->bindPost" in method:return method
                    registers=re.search(r"\.registers (\d+)",method)
                    if not registers or int(registers.group(1))>16:return method
                    aid=re.search(r"invoke-virtual \{([vp]\d+)\}, Lcom/ss/android/ugc/aweme/feed/model/Aweme;->getAid\(\)Ljava/lang/String;",method)
                    if not aid:return method
                    register=aid.group(1)
                    pattern=r"(?m)^([ \t]*if-eqz "+register+r", :[^\n]+)"
                    def bind(m):return "    invoke-static {p0, "+register+"}, "+DATES+"->bindPost(Ljava/lang/Object;Ljava/lang/Object;)V\n"+m.group(1)
                    changed,hits=re.subn(pattern,bind,method,count=1)
                    if hits:counts["visible publication date"]=counts.get("visible publication date",0)+hits
                    return changed
                text=re.sub(r"(?ms)^\.method public final a2\(ILcom/ss/android/ugc/aweme/feed/model/Aweme;\)V.*?^\.end method",selected_date,text)
            if text != before:
                with open(path, "w", encoding="utf-8") as handle:
                    handle.write(text)
    return counts



FLAG_READER = "LX/09tK;"
FLAG_ANSWERS = {
    "Z": ("forcedBoolean", "Ljava/lang/Boolean;", "booleanValue()Z", ""),
    "I": ("forcedInt", "Ljava/lang/Integer;", "intValue()I", ""),
    "J": ("forcedLong", "Ljava/lang/Long;", "longValue()J", "-wide"),
    "F": ("forcedFloat", "Ljava/lang/Float;", "floatValue()F", ""),
    "D": ("forcedDouble", "Ljava/lang/Double;", "doubleValue()D", "-wide"),
    "Ljava/lang/String;": ("forcedString", "Ljava/lang/String;", None, "-object"),
}
FLAG_LABEL = "native flag reader"


def _parameters(descriptor: str) -> List[str]:
    out = []
    at = 0
    while at < len(descriptor):
        start = at
        while descriptor[at] == "[":
            at += 1
        at = descriptor.index(";", at) + 1 if descriptor[at] == "L" else at + 1
        out.append(descriptor[start:at])
    return out


def defines(dex: bytes, descriptor: str) -> bool:
    wanted = descriptor.encode()
    if wanted not in dex or len(dex) < 112:
        return False
    string_ids_off = struct.unpack_from("<I", dex, 60)[0]
    type_ids_off = struct.unpack_from("<I", dex, 68)[0]
    class_defs_size, class_defs_off = struct.unpack_from("<2I", dex, 96)
    for i in range(class_defs_size):
        index = struct.unpack_from("<I", dex, class_defs_off + 32 * i)[0]
        string = struct.unpack_from("<I", dex, type_ids_off + 4 * index)[0]
        at = struct.unpack_from("<I", dex, string_ids_off + 4 * string)[0]
        length, at = _uleb(dex, at)
        if length == len(wanted) and dex[at:at + length] == wanted:
            return True
    return False


def rewrite_flag_reader(root: str, dex: Optional[bytes] = None) -> Dict[str, int]:
    counts: Dict[str, int] = {}
    if dex is not None and not defines(dex, FLAG_READER):
        return counts
    for directory, _, files in os.walk(root):
        for name in files:
            if not re.fullmatch(r"09tK(?:\.\d+)?\.smali", name):
                continue
            path = os.path.join(directory, name)
            with open(path, encoding="utf-8") as handle:
                text = handle.read()
            if not text.startswith(".class") or FLAG_READER not in text.split("\n", 1)[0]:
                continue

            def ask(match):
                head, descriptor, returned, registers, body = match.groups()
                answer = FLAG_ANSWERS.get(returned)
                types = _parameters(descriptor)
                if answer is None or "Ljava/lang/String;" not in types or " static " in head:
                    return match.group(0)
                if FLAGS + "->forced" in body:
                    counts[FLAG_LABEL] = counts.get(FLAG_LABEL, 0) + 1
                    return match.group(0)
                key = 1
                for kind in types[:types.index("Ljava/lang/String;")]:
                    key += 2 if kind in ("J", "D") else 1
                taken = 1 + sum(2 if kind in ("J", "D") else 1 for kind in types)
                if int(registers) - taken < 2:
                    raise RuntimeError("TikTok's experiment reader has no register to spare in %s" % head)
                method, boxed, unbox, wide = answer
                lines = ["    invoke-static/range {p%d .. p%d}, %s->%s(Ljava/lang/String;)%s"
                         % (key, key, FLAGS, method, boxed),
                         "    move-result-object v0",
                         "    if-eqz v0, :soufit_flag"]
                if unbox:
                    lines += ["    invoke-virtual {v0}, %s->%s" % (boxed, unbox),
                              "    move-result%s v0" % wide]
                lines += ["    return%s v0" % wide, "    :soufit_flag"]
                counts[FLAG_LABEL] = counts.get(FLAG_LABEL, 0) + 1
                rows = body.split("\n")
                inside = False
                at = 0
                for at, row in enumerate(rows):
                    word = row.strip()
                    if inside:
                        inside = not word.startswith(".end annotation")
                    elif word.startswith(".annotation"):
                        inside = True
                    elif word and not word.startswith((".prologue", ".param", ".end param")):
                        break
                rows[at:at] = ["\n\n".join(lines) + "\n"]
                return "%s(%s)%s\n    .registers %s\n%s" % (head, descriptor, returned, registers,
                                                             "\n".join(rows))

            changed = re.sub(
                r"(?ms)^(\.method [^\n(]*)\(([^)\n]*)\)(\S+)\n    \.registers (\d+)\n(.*?^\.end method)",
                ask, text)
            if changed != text:
                with open(path, "w", encoding="utf-8") as handle:
                    handle.write(changed)
    return counts


def rewrite_hdr_switch(root: str, dex: Optional[bytes] = None) -> Dict[str, int]:
    counts: Dict[str, int] = {}
    if dex is not None and not defines(dex, PLAYER_CONFIG):
        return counts
    path = os.path.join(root, *PLAYER_CONFIG[1:-1].split("/")) + ".smali"
    if not os.path.exists(path):
        return counts
    with open(path, encoding="utf-8") as handle:
        text = handle.read()
    match = re.search(r"(?ms)^\.method public isForceHdrOff\(\)Z\n    \.registers (\d+)\n(.*?)^\.end method", text)
    if match is None:
        return counts
    counts[HDR_LABEL] = 1
    if HDR_QUESTION in match.group(2):
        return counts
    if int(match.group(1)) < 2:
        raise RuntimeError("TikTok's HDR switch has no register to spare")
    rows = match.group(2).split("\n")
    at = 0
    for at, row in enumerate(rows):
        word = row.strip()
        if word and not word.startswith((".prologue", ".param", ".end param", ".annotation", ".end annotation")):
            break
    rows[at:at] = ["    invoke-static {}, %s\n\n    move-result v0\n\n    if-eqz v0, :soufit_hdr\n\n"
                   "    const/4 v0, 0x1\n\n    return v0\n\n    :soufit_hdr\n" % HDR_QUESTION]
    text = text[:match.start(2)] + "\n".join(rows) + text[match.end(2):]
    with open(path, "w", encoding="utf-8") as handle:
        handle.write(text)
    return counts


SAFELY = "Lbk/soufit/core/Safely;"
for anchor in ("LX/0TFu;", "LX/0TVv;"):
    ANCHORED_SOURCES.append((anchor, "Landroid/content/Context;", "getString",
        "(I)Ljava/lang/String;", "(Landroid/content/Context;I)Ljava/lang/String;", SAFELY))
    CLASS_ANCHORED_SOURCES.add(anchor)

STARTER = "Landroidx/activity/ComponentActivity;"
START_LABEL = "native start"
START_ROUTE = "Lbk/soufit/theme/Launcher;->route(Landroid/content/Intent;)V"


def rewrite_starts(root: str, dex: Optional[bytes] = None) -> Dict[str, int]:
    counts: Dict[str, int] = {}
    if dex is not None and not defines(dex, STARTER):
        return counts
    path = os.path.join(root, *STARTER[1:-1].split("/")) + ".smali"
    if not os.path.exists(path):
        return counts
    with open(path, encoding="utf-8") as handle:
        text = handle.read()

    def shown(match):
        head, body = match.group(1), match.group(2)
        counts[START_LABEL] = counts.get(START_LABEL, 0) + 1
        if START_ROUTE in body:
            return match.group(0)
        rows = body.split("\n")
        inside = False
        at = 0
        for at, row in enumerate(rows):
            word = row.strip()
            if inside:
                inside = not word.startswith(".end annotation")
            elif word.startswith(".annotation"):
                inside = True
            elif word and not word.startswith((".registers", ".prologue", ".param", ".end param")):
                break
        rows[at:at] = ["    invoke-static {p1}, %s\n" % START_ROUTE]
        return head + "\n".join(rows)

    changed = re.sub(
        r"(?ms)^(\.method public startActivityForResult\(Landroid/content/Intent;I(?:Landroid/os/Bundle;)?\)V\n)(.*?^\.end method)",
        shown, text)
    if changed != text:
        with open(path, "w", encoding="utf-8") as handle:
            handle.write(changed)
    return counts


SETTINGS_CELL = "Lbk/soufit/ui/SettingsCell;"
SETTINGS_LABEL = "native settings row"
SETTINGS_ROWS = "LX/0MUp;"
SETTINGS_GROUP = "LX/1Htf;"
SETTINGS_ROW_METHOD = ".method public static final LIZ(LX/0MUl;ZZLX/0Lx7;I)V"
SETTINGS_FIRST_ROW = ("    invoke-static/range {v9 .. v14}, LX/1HtZ;->LIZIZ(ZZLcom/ss/android/ugc/aweme/setting/ui/"
                      "rvmpcompose/group/top/cell/ManagePostsVM;LX/0Lx7;II)V\n")


def _class_file(root: str, descriptor: str) -> Optional[str]:
    path = os.path.join(root, *descriptor[1:-1].split("/")) + ".smali"
    return path if os.path.exists(path) else None


def rewrite_settings_row(root: str, dex: Optional[bytes] = None) -> Dict[str, int]:
    counts: Dict[str, int] = {}
    if dex is None or defines(dex, SETTINGS_ROWS):
        path = _class_file(root, SETTINGS_ROWS)
        if path:
            with open(path, encoding="utf-8") as handle:
                text = handle.read()
            if ".method public static final soufit(LX/0MUl;ZZLX/0Lx7;I)V" in text:
                counts[SETTINGS_LABEL] = counts.get(SETTINGS_LABEL, 0) + 1
            else:
                start = text.find(SETTINGS_ROW_METHOD)
                end = text.find(".end method", start) if start >= 0 else -1
                if end >= 0:
                    body = text[start:end + len(".end method")]
                    copy = body.replace(SETTINGS_ROW_METHOD,
                                        ".method public static final soufit(LX/0MUl;ZZLX/0Lx7;I)V", 1)
                    copy, titled = re.subn(
                        r"    const v0, 0x[0-9a-f]+\n(?:[ \t]*\n|[ \t]*\.line \d+\n)*"
                        r"    invoke-static \{v0, v6\}, LX/0L52;->LIZIZ\(ILX/0Lx7;\)Ljava/lang/String;",
                        "    invoke-static {}, %s->title()Ljava/lang/String;" % SETTINGS_CELL, copy)
                    copy, pictured = re.subn(r"sget-object v0, LX/0LMt;->n:LX/0LPz;",
                                             "sget-object v0, LX/0LMt;->o:LX/0LPz;", copy)
                    copy, again = re.subn(
                        r"    new-instance v0, Lkotlin/jvm/internal/AwS1S0121000_8;\n.*?"
                        r"invoke-direct/range \{v6 \.\. v11\}, Lkotlin/jvm/internal/AwS1S0121000_8;-><init>\(LX/0MUl;ZZII\)V",
                        "    invoke-static {v5, v4, v3, v2}, %s->again(Ljava/lang/Object;ZZI)Ljava/lang/Object;\n\n"
                        "    move-result-object v0\n\n"
                        "    check-cast v0, Lkotlin/jvm/functions/Function2;" % SETTINGS_CELL, copy, flags=re.S)
                    if titled == 1 and pictured == 1 and again == 1:
                        text = text[:end + len(".end method")] + "\n\n" + copy + text[end + len(".end method"):]
                        with open(path, "w", encoding="utf-8") as handle:
                            handle.write(text)
                        counts[SETTINGS_LABEL] = counts.get(SETTINGS_LABEL, 0) + 1
    if dex is None or defines(dex, SETTINGS_GROUP):
        path = _class_file(root, SETTINGS_GROUP)
        if path:
            with open(path, encoding="utf-8") as handle:
                text = handle.read()
            asks = "%s->draw(ZLjava/lang/Object;)Z" % SETTINGS_CELL
            if asks in text:
                counts[SETTINGS_LABEL] = counts.get(SETTINGS_LABEL, 0) + 1
            elif text.count(SETTINGS_FIRST_ROW) == 1:
                text = text.replace(SETTINGS_FIRST_ROW,
                                    "    move v1, v9\n\n"
                                    "    invoke-static {v9, v6}, %s\n\n"
                                    "    move-result v9\n\n" % asks
                                    + SETTINGS_FIRST_ROW
                                    + "\n    move v9, v1\n")
                with open(path, "w", encoding="utf-8") as handle:
                    handle.write(text)
                counts[SETTINGS_LABEL] = counts.get(SETTINGS_LABEL, 0) + 1
    return counts


def carries_an_anchor(dex: bytes) -> bool:
    return (any(anchor.encode() in dex for anchor, *_rest in ANCHORED_SOURCES)
            or FEED_ITEM_LIST.encode() in dex
            or b"Lcom/ss/android/ugc/aweme/comment/longpresspanel/ui/CommentLongPressFragment;" in dex
            or b"LX/0wiL;" in dex or b"Lkotlin/jvm/internal/AwS704S0100000_26;" in dex
            or b"Lcom/ss/android/ugc/aweme/offlinemode/viewmodel/OfflineModeManagerVM;" in dex
            or b"Lcom/ss/android/ugc/aweme/comment/commentlist/api/CommentApi;" in dex
            or b"Lcom/ss/android/ugc/aweme/commentv2/commentlist/CommentApiV2;" in dex
            or b"Lcom/ss/android/ugc/aweme/commentv2/commentlist/CommentApiV2SplitJson;" in dex
            or b"LX/0oah;" in dex
            or b"LX/0wjq;" in dex
            or b"Lcom/ss/android/ugc/aweme/comment/commentlist/api/CommentApi;" in dex
            or b"LX/0oah;" in dex
            or b"Lcom/ss/android/ugc/aweme/commentv2/commentlist/CommentApiV2;" in dex
            or b"Lcom/ss/android/ugc/aweme/commentv2/commentlist/CommentApiV2SplitJson;" in dex
            or b"LX/0wk3;" in dex or b"Lcom/ss/android/ugc/aweme/offlinemode/ui/OfflineModeContentComponent;" in dex
            or b"Lcom/ss/android/ugc/aweme/offlinemode/ui/bottom/OfflineModeDetailBottomComponent;" in dex
            or b"Lcom/ss/android/ugc/aweme/offlinemode/ui/sheet/OfflineModeSheetPageAssem;" in dex
            or b"audio_comment_publish" in dex or b"profile_bg_in_allow_list" in dex
            or defines(dex, FLAG_READER)
            or b"Lcom/ss/android/ugc/aweme/feed/assem/videoauthorinfo/VideoAuthorInfoRelationAssem;" in dex
            or any(owner.encode() in dex for owner in RAIL_CLASSES)
            or PLAYER_CONTROLLER.encode() in dex
            or any(owner.encode() in dex for owner in (REPOST_CLASS,AVATAR_CLASS,PROFILE_CONTAINER,PUSH_GUIDE)+MESSAGE_CLASSES)
            or SPEED_TOUCH_CLASS.encode() in dex or SPEED_EDGE_CLASS.encode() in dex)


def rewrite_speed_touch(root: str) -> Dict[str, int]:
    counts: Dict[str, int] = {}
    found_class = False
    for dirpath, _dirs, files in os.walk(root):
        for name in files:
            if not name.endswith(".smali"):
                continue
            path = os.path.join(dirpath, name)
            with open(path, encoding="utf-8") as handle:
                text = handle.read()
            if not text.startswith(".class "):
                continue
            declaration = text.split("\n", 1)[0]
            edge = SPEED_EDGE_CLASS in declaration
            if not edge and SPEED_TOUCH_CLASS not in declaration:
                continue
            label = "hold speed, edge touch" if edge else "hold speed, feed touch"
            found_class = True
            start = text.find(SPEED_EDGE_METHOD if edge else SPEED_TOUCH_METHOD)
            if start < 0:
                raise RuntimeError("TikTok feed touch callback has changed")
            end = text.find(".end method", start)
            if end < 0:
                raise RuntimeError("TikTok feed touch callback is incomplete")
            method = text[start:end]
            if SPEED_TOUCH_TARGET in method:
                counts[label] = 1
                continue
            anchor = "    :cond_9\n" if edge else "    :cond_27\n"
            if method.count(anchor) != 1:
                raise RuntimeError("TikTok feed touch callback anchor has changed")
            replacement = method.replace(anchor, anchor + SPEED_TOUCH_HOOK, 1)
            text = text[:start] + replacement + text[end:]
            with open(path, "w", encoding="utf-8") as handle:
                handle.write(text)
            counts[label] = 1
    return counts


LOADING = "Lbk/soufit/theme/Loading;"
LOADING_DRAW = re.compile(
    r"\.method public (?:final )?onDraw\(Landroid/graphics/Canvas;\)V\n"
    r"[ \t]*\.registers (\d+)\n(?:[ \t]*\n)*(?:[ \t]*\.prologue\n)?")
LOADING_HOOK = """\
    invoke-static/range {p0 .. p1}, %s->draw(Landroid/view/View;Landroid/graphics/Canvas;)Z

    move-result v0

    if-eqz v0, :soufit_loading_native

    return-void

    :soufit_loading_native
""" % LOADING


def draws_the_loader(dex: bytes) -> bool:
    return b"setProgressBarInfo" in dex


def rewrite_loading(root: str) -> Dict[str, int]:
    counts: Dict[str, int] = {}
    for dirpath, _dirs, files in os.walk(root):
        for name in files:
            if not name.endswith(".smali"):
                continue
            path = os.path.join(dirpath, name)
            with open(path, encoding="utf-8") as handle:
                text = handle.read()
            if "setProgressBarInfo(I)V" not in text:
                continue
            if ("\n.super Landroid/view/View;\n" not in text
                    or "Landroid/graphics/PorterDuffXfermode;" not in text
                    or not re.search(r"(?m)^\.method [^\n]*setProgressBarInfo\(I\)V$", text)):
                continue
            if LOADING in text:
                counts["native loading indicator"] = counts.get("native loading indicator", 0) + 1
                continue
            found = LOADING_DRAW.search(text)
            if found is None or int(found.group(1)) < 3:
                continue
            text = text[:found.end()] + LOADING_HOOK + text[found.end():]
            with open(path, "w", encoding="utf-8") as handle:
                handle.write(text)
            counts["native loading indicator"] = counts.get("native loading indicator", 0) + 1
    return counts


CORNERS = "Lbk/soufit/theme/Corners;"
CARD_CELL = "com/ss/android/ugc/aweme/base/widget/FixedRatioFrameLayout"
CARD_HOOK = "    invoke-static/range {p0 .. p0}, %s->cell(Landroid/view/View;)V\n\n" % CORNERS


def holds_a_card(dex: bytes) -> bool:
    return ("L%s;" % CARD_CELL).encode() in dex


def rewrite_cards(root: str) -> Dict[str, int]:
    counts: Dict[str, int] = {}
    path = os.path.join(root, *CARD_CELL.split("/")) + ".smali"
    if not os.path.isfile(path):
        return counts
    with open(path, encoding="utf-8") as handle:
        text = handle.read()
    if not text.startswith(".class") or ("L%s;" % CARD_CELL) not in text.split("\n", 1)[0]:
        return counts

    def build(match):
        body = match.group(0)
        if CORNERS in body:
            counts["native card corners"] = counts.get("native card corners", 0) + 1
            return body
        changed, hits = re.subn(r"(?m)^[ \t]*return-void", lambda m: CARD_HOOK + m.group(0), body)
        if hits:
            counts["native card corners"] = counts.get("native card corners", 0) + 1
        return changed

    changed = re.sub(r"(?ms)^\.method public constructor <init>\(.*?^\.end method", build, text)
    if changed != text:
        with open(path, "w", encoding="utf-8") as handle:
            handle.write(changed)
    return counts


def touches_a_model(dex: bytes) -> bool:
    for owner, name, _original, _replacement, _target in MODEL_SOURCES + MODEL_STATICS:
        theirs = name[0] if isinstance(name, tuple) else name
        if owner.encode() in dex and theirs.encode() in dex:
            return True
    for owner, field, _kind, _name, _target in FIELD_SOURCES:
        if owner.encode() in dex and field.encode() in dex:
            return True
    for name, _original, _replacement, _target in WILD_SOURCES:
        if name.encode() in dex:
            return True
    for label, _descriptor, _ours, _target in DISCOVERED_STATICS + DISCOVERED_VIRTUALS:
        found = FOUND.get(label)
        if found and found[0].encode() in dex and found[1].encode() in dex:
            return True
    return False


def reads_a_colour(dex: bytes) -> bool:
    for owner, name, _original, _replacement in COLOUR_SOURCES:
        if owner.encode() in dex and name.encode() in dex:
            return True
    for owner, name, _signature in COLOUR_STATICS:
        if owner.encode() in dex and name.encode() in dex:
            return True
    return False


def holds_the_pink(dex: bytes) -> bool:
    needle = struct.pack("<I", TIKTOK_PINK)
    at = dex.find(needle)
    while at != -1:
        if at >= 2 and dex[at - 2] == 0x14:
            return True
        at = dex.find(needle, at + 1)
    return False


def _required(pattern) -> str:
    try:
        from re import _parser as parser
    except ImportError:
        import sre_parse as parser
    if pattern.flags & re.IGNORECASE:
        return ""
    best = run = ""
    try:
        pieces = list(parser.parse(pattern.pattern, pattern.flags))
    except Exception:
        return ""
    for kind, value in pieces + [(None, None)]:
        if str(kind) == "LITERAL":
            run += chr(value)
            continue
        if len(run) > len(best):
            best = run
        run = ""
    return best


def _literal_of(pattern) -> str:
    source = pattern.pattern
    for head in (r"(\{[^}]*\}), ", r"^(\s*)const ([vp]\d+), "):
        at = source.find(head)
        if at != -1:
            tail = source[at + len(head):]
            if tail.endswith("$"):
                tail = tail[:-1]
            return re.sub(r"\\(.)", r"\1", tail)
    return ""


def rewrite_accent(root: str) -> Dict[str, int]:
    counts: Dict[str, int] = {}
    prepared = accent_rules()
    markers = [_literal_of(pattern) for _label, pattern, _target in prepared]
    feed_animation = "LX/1HkT;->setAnimation(Ljava/lang/String;)V"
    for dirpath, _dirs, files in os.walk(root):
        for name in files:
            if not name.endswith(".smali"):
                continue
            path = os.path.join(dirpath, name)
            with open(path, encoding="utf-8") as handle:
                text = handle.read()
            before = text
            for (label, pattern, target), marker in zip(prepared, markers):
                if marker and marker not in text:
                    continue
                if label == "feed Lottie setAnimation" and feed_animation not in text:
                    continue
                text, hits = pattern.subn(target, text)
                if hits:
                    counts[label] = counts.get(label, 0) + hits
            if text != before:
                with open(path, "w", encoding="utf-8") as handle:
                    handle.write(text)
    return counts


def force_false(root: str) -> Dict[str, int]:
    counts: Dict[str, int] = {}
    for class_name, signature in FORCED_FALSE:
        path = os.path.join(root, *class_name.split("/")) + ".smali"
        if not os.path.exists(path):
            continue
        with open(path, encoding="utf-8") as handle:
            text = handle.read()
        pattern = re.compile(
            r"^\.method ([^\n]*%s)\n.*?^\.end method$" % re.escape(signature),
            re.MULTILINE | re.DOTALL,
        )
        match = pattern.search(text)
        if match is None:
            raise RuntimeError(
                "%s is in the apk but has no %s to rewrite -- the fallback this "
                "depends on has moved, and Google sign-in would be dead on arrival"
                % (class_name, signature)
            )
        stub = ".method %s\n    .registers 1\n\n    const/4 v0, 0x0\n\n    return v0\n.end method" % (
            match.group(1),
        )
        text = text[: match.start()] + stub + text[match.end():]
        with open(path, "w", encoding="utf-8") as handle:
            handle.write(text)
        counts["%s->%s" % (class_name.rsplit("/", 1)[-1], signature)] = 1
    return counts


def interesting(dex: bytes, literals: Optional[Dict[str, str]] = None) -> bool:
    for old in (literals or {}):
        if old.encode() in dex:
            return True
    if (b"tt_feed_like_tap_tux_lottie.json" in dex
            or b"tt_feed_like_tap_quick_tux_lottie.json" in dex
            or b"feed_fill_like/feed_fill_like_add_lottie.json" in dex):
        return True
    if (holds_the_pink(dex) or reads_a_colour(dex) or touches_a_model(dex)
            or carries_an_anchor(dex) or draws_the_loader(dex) or holds_a_card(dex)):
        return True
    for class_name, _signature in FORCED_FALSE:
        if ("L%s;" % class_name).encode() in dex:
            return True
    if (defines(dex, PLAYER_CONFIG) or defines(dex, SETTINGS_ROWS) or defines(dex, SETTINGS_GROUP)
            or defines(dex, STARTER)):
        return True
    if TELEPHONY.encode() not in dex:
        return False
    return any(name.encode() in dex for name, _o, _r in TARGETS)


def rewrite_literals(root: str, literals: Dict[str, str]) -> Dict[str, int]:
    counts: Dict[str, int] = {}
    if not literals:
        return counts
    for dirpath, _dirs, files in os.walk(root):
        for name in files:
            if not name.endswith(".smali"):
                continue
            path = os.path.join(dirpath, name)
            with open(path, encoding="utf-8") as handle:
                text = handle.read()
            before = text
            for old, new in literals.items():
                needle = '"%s"' % old
                hits = text.count(needle)
                if hits:
                    text = text.replace(needle, '"%s"' % new)
                    counts[old] = counts.get(old, 0) + hits
            if text != before:
                with open(path, "w", encoding="utf-8") as handle:
                    handle.write(text)
    return counts


def rewrite_smali(root: str) -> Dict[str, int]:
    counts: Dict[str, int] = {}
    prepared = rules()
    for dirpath, _dirs, files in os.walk(root):
        for name in files:
            if not name.endswith(".smali"):
                continue
            path = os.path.join(dirpath, name)
            with open(path, encoding="utf-8") as handle:
                text = handle.read()
            if TELEPHONY not in text:
                continue
            before = text
            for label, pattern, target in prepared:
                text, hits = pattern.subn(target, text)
                if hits:
                    counts[label] = counts.get(label, 0) + hits
            if text != before:
                with open(path, "w", encoding="utf-8") as handle:
                    handle.write(text)
    return counts


class Smali:

    def __init__(self, jar: str, api: int, jobs: int = 0, heap: str = "4g"):
        self.jar = jar
        self.api = api
        self.jobs = jobs or int(os.environ.get("SOUFIT_JOBS") or 0) or (os.cpu_count() or 2)
        self.heap = os.environ.get("SOUFIT_HEAP") or heap

    def _run(self, main: str, args: List[str]) -> None:
        command = ["java", "-Xmx" + self.heap, "-cp", self.jar, main] + args
        result = subprocess.run(command, capture_output=True, text=True)
        if result.returncode != 0:
            raise RuntimeError(
                "%s failed:\n%s\n%s" % (main.split(".")[-2], result.stdout, result.stderr)
            )

    def disassemble(self, dex_path: str, out_dir: str) -> None:
        self._run(
            "com.android.tools.smali.baksmali.Main",
            ["d", "-a", str(self.api), "-j", str(self.jobs), "-o", out_dir, dex_path],
        )

    def assemble(self, smali_dir: str, dex_path: str) -> None:
        self._run(
            "com.android.tools.smali.smali.Main",
            ["a", "-a", str(self.api), "-j", str(self.jobs), "-o", dex_path, smali_dir],
        )


def dex_format(dex: bytes) -> str:
    return dex[4:7].decode("ascii", "replace")




def rewrite_targets() -> List[str]:
    out = ["%s->accent()I" % ACCENT]
    out.append("%s->setAnimation(Ljava/lang/Object;Ljava/lang/String;)V" % ACCENT)
    for name, _original, replacement in TARGETS:
        out.append("%s->%s%s" % (REGION, name, replacement))
    for _owner, name, _original, replacement in COLOUR_SOURCES:
        out.append("%s->%s%s" % (ACCENT, name, replacement))
    for _owner, name, signature in COLOUR_STATICS:
        out.append("%s->%s%s" % (ACCENT, name, signature))
    for _owner, name, _original, replacement, target in MODEL_SOURCES + MODEL_STATICS:
        _theirs, ours = name if isinstance(name, tuple) else (name, name)
        out.append("%s->%s%s" % (target, ours, replacement))
    for owner, _field, kind, name, target in FIELD_SOURCES:
        out.append("%s->%s(%s)%s" % (target, name, owner, kind))
    for _anchor, _owner, name, _original, replacement, target in ANCHORED_SOURCES:
        out.append("%s->%s%s" % (target, name, replacement))
    out.append("Lbk/soufit/feed/VideoActions;->captureShare(Ljava/lang/Object;)V")
    out.extend(["Lbk/soufit/feed/OfflineComments;->closed(Ljava/lang/Object;)V", "Lbk/soufit/feed/OfflineComments;->nativeTask(Ljava/lang/String;)Ljava/lang/Object;", "Lbk/soufit/feed/OfflineComments;->nativeObservable(Ljava/lang/String;)Ljava/lang/Object;"])
    out.extend(["Lbk/soufit/feed/OfflineComments;->closed(Ljava/lang/Object;)V", "Lbk/soufit/feed/OfflineComments;->nativeTask(Ljava/lang/String;)Ljava/lang/Object;", "Lbk/soufit/feed/OfflineComments;->nativeObservable(Ljava/lang/String;)Ljava/lang/Object;"])
    out.extend(["Lbk/soufit/feed/OfflineChoices;->counts(Ljava/util/List;)Ljava/util/List;", "Lbk/soufit/feed/OfflineChoices;->row(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", "Lbk/soufit/feed/OfflineChoices;->validCount(Ljava/util/List;Ljava/lang/Object;)Z", "Lbk/soufit/feed/OfflineChoices;->legacy(Ljava/lang/Object;)V", "Lbk/soufit/feed/OfflineComments;->cached(Ljava/lang/Object;Ljava/lang/String;)V", "Lbk/soufit/feed/OfflineComments;->content(Ljava/lang/Object;)V", "Lbk/soufit/feed/OfflineComments;->bottom(Landroid/view/View;)Landroid/view/View;"])
    out.append(SPEED_TOUCH_TARGET)
    out.append(COMMENTS+"->mark(Landroid/view/View;)V")
    out.append(COMMENTS+"->openNativeActions(Ljava/lang/Object;)Z")
    out.append(COMMENTS+"->nativeItems(Ljava/lang/Object;Ljava/util/List;)Ljava/util/List;")
    out.append(FEED+"->withoutAds(Ljava/util/List;)Ljava/util/List;")
    out.append("Lbk/soufit/feed/Repost;->textureChanged(Landroid/view/View;)V")
    out.append("Lbk/soufit/theme/Accent;->dislike(Ljava/lang/Object;)V")
    out.append(COMMENTS+"->bindCell(Ljava/lang/Object;)V")
    out.append(RAIL+"->bindNative(Ljava/lang/Object;)V")
    out.append("Lbk/soufit/feed/PlayerTools;->resumed(Ljava/lang/Object;Ljava/lang/String;)V")
    out.extend(["Lbk/soufit/feed/Repost;->bind(Ljava/lang/Object;)V","Lbk/soufit/feed/Repost;->anchor(Ljava/lang/Object;)V","Lbk/soufit/profile/ProfileLayout;->bind(Ljava/lang/Object;Landroid/view/View;)V","Lbk/soufit/social/MessageTimes;->before(Ljava/lang/Object;)V","Lbk/soufit/social/MessageTimes;->bind(Ljava/lang/Object;Ljava/lang/Object;)V","Lbk/soufit/social/ModNotifications;->noPrompts()Z"])
    out.extend("%s->%s(Ljava/lang/String;)%s" % (FLAGS, method, boxed)
               for method, boxed, _unbox, _wide in FLAG_ANSWERS.values())
    out.append(HDR_QUESTION)
    out.append(START_ROUTE)
    out.extend(["%s->title()Ljava/lang/String;" % SETTINGS_CELL,
                "%s->again(Ljava/lang/Object;ZZI)Ljava/lang/Object;" % SETTINGS_CELL,
                "%s->draw(ZLjava/lang/Object;)Z" % SETTINGS_CELL])
    out.append(CORNERS + "->cell(Landroid/view/View;)V")
    out.append(LOADING + "->draw(Landroid/view/View;Landroid/graphics/Canvas;)Z")
    out.append(DATES+"->bindAuthor(Ljava/lang/Object;)V")
    out.append(DATES+"->bindPost(Ljava/lang/Object;Ljava/lang/Object;)V")
    for name, _original, replacement, target in WILD_SOURCES:
        out.append("%s->%s%s" % (target, name, replacement))
    for label, descriptor, ours, target in DISCOVERED_STATICS:
        if label in FOUND:
            out.append("%s->%s%s" % (target, ours, descriptor))
    for label, descriptor, ours, target in DISCOVERED_VIRTUALS:
        if label in FOUND:
            out.append("%s->%s%s"
                       % (target, ours, "(Ljava/lang/Object;" + descriptor[1:]))
    return out


def _uleb(data: bytes, at: int) -> Tuple[int, int]:
    value = shift = 0
    while True:
        byte = data[at]
        at += 1
        value |= (byte & 0x7F) << shift
        shift += 7
        if not byte & 0x80:
            return value, at


def defined_methods(dex: bytes) -> set:
    string_ids_off = struct.unpack_from("<I", dex, 60)[0]
    type_ids_off = struct.unpack_from("<I", dex, 68)[0]
    proto_ids_off = struct.unpack_from("<I", dex, 76)[0]
    method_ids_off = struct.unpack_from("<I", dex, 92)[0]
    class_defs_size, class_defs_off = struct.unpack_from("<2I", dex, 96)

    def string(index: int) -> str:
        at = struct.unpack_from("<I", dex, string_ids_off + 4 * index)[0]
        length, at = _uleb(dex, at)
        return dex[at:at + length].decode("utf-8", "replace")

    def type_name(index: int) -> str:
        return string(struct.unpack_from("<I", dex, type_ids_off + 4 * index)[0])

    def descriptor(index: int) -> str:
        _shorty, return_type, parameters = struct.unpack_from(
            "<3I", dex, proto_ids_off + 12 * index)
        arguments = ""
        if parameters:
            count = struct.unpack_from("<I", dex, parameters)[0]
            arguments = "".join(
                type_name(struct.unpack_from("<H", dex, parameters + 4 + 2 * i)[0])
                for i in range(count)
            )
        return "(%s)%s" % (arguments, type_name(return_type))

    def signature(index: int) -> str:
        owner, proto, name = struct.unpack_from("<HHI", dex, method_ids_off + 8 * index)
        return "%s->%s%s" % (type_name(owner), string(name), descriptor(proto))

    out = set()
    for i in range(class_defs_size):
        data_off = struct.unpack_from("<I", dex, class_defs_off + 32 * i + 24)[0]
        if not data_off:
            continue
        counts = []
        at = data_off
        for _ in range(4):
            value, at = _uleb(dex, at)
            counts.append(value)
        for _ in range(counts[0] + counts[1]):
            _diff, at = _uleb(dex, at)
            _flags, at = _uleb(dex, at)
        for methods in (counts[2], counts[3]):
            index = 0
            for step in range(methods):
                diff, at = _uleb(dex, at)
                _flags, at = _uleb(dex, at)
                _code, at = _uleb(dex, at)
                index = diff if step == 0 else index + diff
                out.add(signature(index))
    return out


def find_statics(dexes: Dict[str, bytes]) -> Dict[str, Tuple[str, str]]:
    wanted = {descriptor: label for label, descriptor, _ours, _target
              in DISCOVERED_STATICS + DISCOVERED_VIRTUALS}
    seen: Dict[str, set] = {label: set() for label in wanted.values()}

    for dex in dexes.values():
        for owner, name, descriptor in _static_methods(dex):
            label = wanted.get(descriptor)
            if label is not None:
                seen[label].add((owner, name))

    out: Dict[str, Tuple[str, str]] = {}
    for label, found in seen.items():
        if len(found) == 1:
            out[label] = next(iter(found))
    return out


def _static_methods(dex: bytes):
    strings = struct.unpack_from("<I", dex, 60)[0]
    types = struct.unpack_from("<I", dex, 68)[0]
    protos = struct.unpack_from("<I", dex, 76)[0]
    count, methods = struct.unpack_from("<2I", dex, 88)

    def text(index: int) -> str:
        at = struct.unpack_from("<I", dex, strings + 4 * index)[0]
        size, at = _uleb(dex, at)
        return dex[at:at + size].decode("utf-8", "replace")

    def kind(index: int) -> str:
        return text(struct.unpack_from("<I", dex, types + 4 * index)[0])

    def shape(index: int) -> str:
        _shorty, ret, params = struct.unpack_from("<3I", dex, protos + 12 * index)
        taken = ""
        if params:
            how_many = struct.unpack_from("<I", dex, params)[0]
            taken = "".join(kind(struct.unpack_from("<H", dex, params + 4 + 2 * i)[0])
                            for i in range(how_many))
        return "(%s)%s" % (taken, kind(ret))

    for i in range(count):
        owner, proto, name = struct.unpack_from("<HHI", dex, methods + 8 * i)
        yield kind(owner), text(name), shape(proto)


def missing_targets(dex: bytes) -> List[str]:
    defined = defined_methods(dex)
    return [target for target in rewrite_targets() if target not in defined]


def rewrite_everything(root: str, literals: Optional[Dict[str, str]] = None,
                       dex: Optional[bytes] = None) -> Dict[str, int]:
    counts = rewrite_smali(root)
    counts.update(rewrite_literals(root, literals or {}))
    counts.update(force_false(root))
    counts.update(rewrite_models(root))
    counts.update(rewrite_flag_reader(root, dex))
    counts.update(rewrite_hdr_switch(root, dex))
    counts.update(rewrite_settings_row(root, dex))
    counts.update(rewrite_starts(root, dex))
    counts.update(rewrite_anchored(root))
    counts.update(rewrite_accent(root))
    counts.update(rewrite_speed_touch(root))
    if dex is None or draws_the_loader(dex):
        counts.update(rewrite_loading(root))
    counts.update(rewrite_cards(root))
    return counts


def next_dex_name(names: List[str]) -> str:
    used = set()
    for name in names:
        match = re.fullmatch(r"classes(\d*)\.dex", name)
        if match:
            used.add(int(match.group(1) or "1"))
    number = 2
    while number in used:
        number += 1
    return "classes%d.dex" % number
