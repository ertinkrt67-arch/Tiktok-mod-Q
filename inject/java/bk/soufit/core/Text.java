package bk.soufit.core;
import java.util.Locale;
public final class Text {
    private Text() {} private static final boolean RU = "ru".equals(language()) || "be".equals(language());
    private static final boolean UK = "uk".equals(language());
    public static final String PINNED_FRIENDS = pick("Закреплённые друзья", "Закріплені друзі", "Pinned friends");
    public static final String FRIEND_CANDIDATES = pick("Друзья", "Друзі", "Friends");
    public static final String MOVE_UP = pick("Выше", "Вище", "Move up");
    public static final String MOVE_DOWN = pick("Ниже", "Нижче", "Move down");
    public static final String UNPIN = pick("Открепить", "Відкріпити", "Unpin");
    public static final String PIN_LIMIT = pick("Можно закрепить до 20 друзей", "Можна закріпити до 20 друзів", "Pin up to 20 friends");
    public static final String FRIENDS_LOAD_HINT = pick("Сначала откройте список друзей TikTok. Из него появятся доступные профили.",
            "Спочатку відкрийте список друзів TikTok.", "Open the TikTok friends list first to load available profiles.");
    public static final String NOTHING_FOUND = pick("Ничего не найдено", "Нічого не знайдено", "No matches");
    public static final String PROFILE_LAYOUT = pick("Вид профиля", "Вигляд профілю", "Profile layout");
    public static final String LAYOUT_NATIVE = pick("Обычный", "Звичайний", "Original");
    public static final String LAYOUT_COMPACT = pick("Компактный", "Компактний", "Compact");
    public static final String LAYOUT_EXPANDED = pick("Расширенный", "Розширений", "Expanded");
    public static final String MESSAGE_TIMES = pick("Время сообщений", "Час повідомлень", "Message timestamps");
    public static final String NOTIFICATIONS = pick("Уведомления", "Сповіщення", "Notifications");
    public static final String NOTIFY_MESSAGES = pick("Сообщения", "Повідомлення", "Messages");
    public static final String NOTIFY_PROMOS = pick("Промоуведомления", "Промосповіщення", "Promotional notifications");
    public static final String NOTIFY_OTHER = pick("Другие уведомления", "Інші сповіщення", "Other notifications");
    public static final String NOTIFY_NO_PROMPTS = pick("Без напоминаний о разрешении", "Без нагадувань про дозвіл", "No permission reminders");
    public static final String NOTIFY_SETTINGS = pick("Разрешение Android", "Дозвіл Android", "Android permission");
    public static final String REPOST = pick("Репост", "Репост", "Repost");
    public static final String REPOST_POSITION = pick("Расположение репоста", "Розташування репосту", "Repost placement");
    public static final String REPOST_FAVORITE = pick("Вместо избранного", "Замість обраного", "Replace favorite");
    public static final String REPOST_AVATAR = pick("Над аватаром", "Над аватаром", "Above avatar");
    public static final String REPOST_FREE = pick("Рядом с избранным", "Поруч з обраним", "Beside favorite");
    public static final String REPOST_NOT_READY = pick("Штатный репост недоступен", "Штатний репост недоступний", "Native repost is unavailable");
    public static final String BLOCK_SUGGESTION = pick("Скрыть такие предложения", "Приховати такі пропозиції", "Hide this suggestion type");
    public static final String VIDEO_ACTIONS = pick("Действия видео", "Дії відео", "Video actions");
    public static final String VIDEO_NOT_READY = pick("Откройте видео в ленте", "Відкрийте відео у стрічці", "Open a feed video first");
    public static final String SAVE_VIDEO = pick("Скачать видео", "Завантажити відео", "Download video");
    public static final String SAVE_AUDIO = pick("Скачать аудио", "Завантажити аудіо", "Download audio");
    public static final String SAVE_IMAGE = pick("Скачать фото", "Завантажити фото", "Download photo");
    public static final String SAVE_FRAME = pick("Сохранить кадр", "Зберегти кадр", "Save current frame");
    public static final String FRAME_NOT_AVAILABLE = pick("Не удалось получить кадр: поверхность недоступна", "Не вдалося отримати кадр: поверхня недоступна",
            "Cannot capture this player surface");
    public static final String MEDIA_NOT_ALLOWED = pick("Автор ограничил скачивание", "Автор обмежив завантаження", "The author restricted downloads");
    public static final String AUDIO_NOT_AVAILABLE = pick("В этом видео нет отдельной аудиодорожки", "У цьому відео немає окремої аудіодоріжки",
            "No separate audio track is available");
    public static final String PHOTO_NOT_AVAILABLE = pick("Фото недоступно для скачивания", "Фото недоступне для завантаження",
            "Photo is unavailable");
    public static final String MEDIA_FAILED = pick("Не удалось сохранить файл. Подробности в диагностике", "Не вдалося зберегти файл. Подробиці в діагностиці",
            "Could not save the file. See diagnostics");
    public static final String DOWNLOADING = pick("Загрузка", "Завантаження", "Downloading");
    public static final String PLAYER_SPEED = pick("Скорость удержания", "Швидкість утримання", "Hold speed");
    public static final String CUSTOM_SPEED = pick("Своё значение", "Власне значення", "Custom value");
    public static final String SPEED_RANGE = pick("Допустимая скорость: 0.5–3×", "Допустима швидкість: 0.5–3×", "Speed must be between 0.5 and 3×");
    public static final String PLAYER_NOT_READY = pick("Плеер ещё не готов", "Плеєр ще не готовий", "The player is not ready yet");
    public static final String SEEK_BACK = pick("Назад на выбранный шаг", "Назад на вибраний крок", "Seek back one step");
    public static final String SEEK_FORWARD = pick("Вперёд на выбранный шаг", "Уперед на вибраний крок", "Seek forward one step");
    public static final String SEEK_SMOOTH = pick("Плавно назад", "Плавно назад", "Smooth rewind");
    public static final String SEEK_STEP = pick("Шаг перемотки, секунды", "Крок перемотування, секунди", "Seek step, seconds");
    public static final String REVERSE_SPEED = pick("Скорость перемотки назад", "Швидкість перемотування назад", "Rewind rate");
    public static final String BLOCK_AUTHOR = pick("Не предлагать этого автора", "Не пропонувати цього автора", "Hide this author");
    public static final String BLOCK_LIMIT = pick("В списке уже 50 авторов", "У списку вже 50 авторів", "The list already contains 50 authors");
    public static final String HIDE_STORIES = pick("Скрывать истории", "Приховувати історії", "Hide stories");
    public static final String HIDE_SUGGESTIONS = pick("Скрывать карточки рекомендаций", "Приховувати картки рекомендацій",
            "Hide recommendation cards");
    public static final String SKIP_LOGIN_GATE = pick("Пропускать принудительный вход", "Пропускати примусовий вхід", "Skip force-login prompts");
    public static final String SKIP_LOGIN_DOB = pick("Авtozаполнение даты рождения", "Автозаповнення дати народження", "Auto-fill birthday");
    public static final String SKIP_LOGIN_PILL = pick("Пропустить", "Пропустити", "Skip");
    public static final String SKIP_DOB_PILL = pick("Пропустить (случайная дата)", "Пропустити (випадкова дата)", "Skip (random DOB)");
    public static final String DL_QUALITY = pick("Качество загрузки", "Якість завантаження", "Download quality");
    public static final String DL_NATIVE = pick("Штатное", "Стандартна", "Original");
    public static final String DL_MAX = pick("Максимальное доступное", "Максимальна доступна", "Highest available");
    public static final String DL_LOW = pick("Минимальное доступное", "Мінімальна доступна", "Lowest available");
    public static final String DL_AUDIO = pick("Только звук", "Лише звук", "Audio only");
    public static final String FRAME_QUALITY = pick("Размер кадра", "Розмір кадру", "Frame size");
    public static final String VIDEO_ACTIONS_HINT = pick("Удерживайте «Отправить» на видео, чтобы открыть действия soufiT.",
            "Утримуйте «Надіслати» на відео, щоб відкрити дії soufiT.", "Hold Share on a video to open soufiT actions.");
    public static final String APPLY = pick("Применить", "Застосувати", "Apply");
    public static final String RAIL = pick("Кнопки видео", "Кнопки відео", "Video buttons");
    public static final String RAIL_LIKE = pick("Лайк", "Вподобайка", "Like");
    public static final String RAIL_COMMENT = pick("Комментарии", "Коментарі", "Comments");
    public static final String RAIL_FAV = pick("Избранное", "Обране", "Favourites");
    public static final String RAIL_SHARE = pick("Отправить", "Надіслати", "Share");
    public static final String COMMENT_COPY_SELECTED = pick("Копировать выделенное", "Копіювати виділене", "Copy selection");
    public static final String COMMENT_SELECT_HINT = pick("Выделите нужный фрагмент текста", "Виділіть потрібний фрагмент тексту",
            "Select a text fragment first");
    public static final String HIDDEN_ITEMS = pick("Что скрыто", "Що приховано", "Hidden items");
    public static final String HIDDEN_ITEMS_NOTE = pick("Вернуть элементы в ленту", "Повернути елементи до стрічки", "Restore feed items");
    public static final String HIDDEN_EMPTY = pick("Ничего не скрыто", "Нічого не приховано", "Nothing is hidden");
    public static final String HIDDEN_RESTORE = pick("Вернуть", "Повернути", "Restore");
    public static final String HIDDEN_RESTORE_ALL = pick("Вернуть всё", "Повернути все", "Restore all");
    public static final String HIDDEN_NEXT_PAGE = pick("Изменения применятся при следующей загрузке ленты.", "Зміни застосуються під час наступного завантаження стрічки.",
            "Changes apply when the feed next loads.");
    public static final String ROW = pick("Настройки мода", "Налаштування моду", "Mod settings");
    public static final String DIAGNOSTICS = pick("Диагностика", "Діагностика", "Diagnostics");
    public static final String COPY_DIAGNOSTICS = pick("Копировать диагностику", "Копіювати діагностику", "Copy diagnostics");
    public static final String PROFILE_STYLE = pick("Оформление профиля", "Оформлення профілю", "Profile style");
    public static final String APPEARANCE = pick("Внешний вид", "Зовнішній вигляд", "Appearance");
    public static final String PROFILE_PREVIEW = pick("Так значок будет выглядеть у ника", "Так значок виглядатиме біля імені",
            "Badge preview beside nickname");
    public static final String PROFILE_PREVIEW_NICK = pick("@ваш_ник", "@ваше_ім’я", "@your_name");
    public static final String PROFILE_PREVIEW_EMPTY = pick("Реальный профиль TikTok", "Справжній профіль TikTok", "Your real TikTok profile");
    public static final String PROFILE_PREVIEW_LOADING = pick("Откройте свой профиль TikTok — имя и фото появятся здесь",
            "Відкрийте свій профіль TikTok — ім’я та фото з’являться тут", "Open your TikTok profile to load its real name and photo");
    public static final String PROFILE_PREVIEW_REAL = pick("Предпросмотр вашего профиля TikTok", "Попередній перегляд вашого профілю TikTok",
            "Preview of your TikTok profile");
    public static final String PROFILE_EYEDROPPER = pick("Пипетка: коснитесь изображения значка, чтобы взять цвет", "Піпетка: торкніться зображення значка, щоб взяти колір",
            "Eyedropper: tap the badge image to sample a colour");
    public static final String PROFILE_PICK_COLOUR = pick("Выберите цвет", "Виберіть колір", "Choose colour");
    public static final String PROFILE_BADGE = pick("Значок у ника", "Значок біля імені", "Badge beside nickname");
    public static final String PROFILE_BADGE_REMOVE = pick("Удалить значок", "Видалити значок", "Remove badge");
    public static final String PROFILE_CROP_X = pick("Положение по горизонтали", "Положення по горизонталі", "Horizontal position");
    public static final String PROFILE_CROP_Y = pick("Положение по вертикали", "Положення по вертикалі", "Vertical position");
    public static final String PROFILE_CROP_ZOOM = pick("Масштаб значка", "Масштаб значка", "Badge scale");
    public static final String PROFILE_GRADIENT = pick("Градиент ника", "Градієнт імені", "Nickname gradient");
    public static final String PROFILE_START = pick("Первый цвет", "Перший колір", "First colour");
    public static final String PROFILE_END = pick("Второй цвет", "Другий колір", "Second colour");
    public static final String PROFILE_MIDDLE = pick("Центральный цвет", "Центральний колір", "Middle colour");
    public static final String PROFILE_THREE = pick("Градиент из трёх цветов", "Градієнт із трьох кольорів", "Three-colour gradient");
    public static final String PROFILE_DIRECTION = pick("Направление градиента", "Напрямок градієнта", "Gradient direction");
    public static final String PROFILE_DIRECTION_RIGHT = pick("Слева направо", "Зліва направо", "Left to right");
    public static final String PROFILE_DIRECTION_DOWN = pick("Сверху вниз", "Згори вниз", "Top to bottom");
    public static final String PROFILE_DIRECTION_DIAGONAL = pick("По диагонали", "По діагоналі", "Diagonal");
    public static final String PROFILE_DIRECTION_REVERSE = pick("Диагональ в обратную сторону", "Діагональ у зворотний бік",
            "Reverse diagonal");
    public static final String PROFILE_BOLD = pick("Полужирный ник", "Напівжирне ім’я", "Bold nickname");
    public static final String PROFILE_GLOW = pick("Мягкое свечение", "М’яке сяйво", "Subtle glow");
    public static final String PROFILE_ANIMATED = pick("Анимировать градиент", "Анімувати градієнт", "Animate gradient");
    public static final String PROFILE_SPEED = pick("Скорость градиента", "Швидкість градієнта", "Gradient speed");
    public static final String PROFILE_SHARE = pick("Показывать стиль другим soufiT", "Показувати стиль іншим soufiT", "Share style with other soufiT users");
    public static final String PROFILE_SHARE_WAIT = pick("Общий стиль станет доступен после подключения HTTPS-сервера soufiT",
            "Спільний стиль стане доступним після підключення HTTPS-сервера soufiT", "Shared style needs a soufiT HTTPS service");
    public static final String STORE_OFFLINE = pick("Магазин плагинов недоступен без HTTPS-сервера", "Магазин плагінів недоступний без HTTPS-сервера",
            "Plugin store needs an HTTPS service");
    public static final String PROFILE_RESET = pick("Сбросить оформление", "Скинути оформлення", "Reset profile style");
    public static final String PROFILE_BADGE_FAILED = pick("Не удалось прочитать изображение", "Не вдалося прочитати зображення",
            "Could not read the image");
    public static final String THEME_PRESET_DARK = pick("Классический пресет", "Класичний пресет", "Classic preset");
    public static final String THEME_PRESET_BLUE = pick("Синий пресет", "Синій пресет", "Blue preset");
    public static final String THEME_RESET = pick("Сбросить к теме TikTok", "Скинути до теми TikTok", "Reset to TikTok theme");
    public static final String THEME_PRESETS = pick("Готовые темы", "Готові теми", "Theme presets");
    public static final String THEME_PRESETS_NOTE = pick("Классическая, синяя или исходная тема TikTok", "Класична, синя або початкова тема TikTok",
            "Classic, blue or original TikTok theme");
    public static final String OPEN_TTCUZ_SITE = pick("Инструкции на сайте soufiT", "Інструкції на сайті soufiT", "Instructions on the soufiT site");
    public static final String THEME_COLOURS = pick("Цвета интерфейса", "Кольори інтерфейсу", "Interface colours");
    public static final String THEME_COLOURS_NOTE = pick("Цвета сохраняются отдельно для светлой и тёмной темы. Применение зависит от имён элементов в этой версии TikTok.",
            "Кольори зберігаються окремо для світлої й темної теми. Застосування залежить від назв елементів у цій версії TikTok.",
            "Colours are saved separately for light and dark mode. Applying them depends on view names in this TikTok version.");
    public static final String REGION = pick("Регион", "Регіон", "Region");
    public static final String CHANGE_REGION = pick("Менять регион", "Змінювати регіон", "Change the region");
    public static final String COUNTRY = pick("Страна", "Країна", "Country");
    public static final String ACCENT = pick("Акцент", "Акцент", "Accent");
    public static final String ACCENT_COLOUR = pick("Акцент", "Акцент", "Accent");
    public static final String LIKE_COLOUR = pick("Цвет лайков", "Колір вподобань", "Like colour");
    public static final String LIKE_RESET = pick("Цвет TikTok по умолчанию", "Типовий колір TikTok", "TikTok default colour");
    public static final String CUSTOM_COLOUR = pick("Свой цвет #RRGGBB", "Свій колір #RRGGBB", "Custom colour #RRGGBB");
    public static final String ACCENT_PICKER = pick("Выберите цвет акцента", "Виберіть колір акценту", "Choose accent colour");
    public static final String ACCENT_NOTE = pick("Меняет розовый, которым TikTok рисует лайки, кнопки и вкладки. " + "Часть значков нарисована картинками — их цвет задаётся при сборке "
            + "и здесь не меняется.", "Змінює рожевий, яким TikTok малює лайки, кнопки та вкладки. " + "Частина значків намальована картинками — їхній колір задається "
            + "під час збірки і тут не змінюється.", "Changes the pink TikTok draws likes, buttons and tabs with. Some icons "
            + "are pictures rather than code; their colour is settled at build " + "time and does not follow.");
    public static final String RESTART_PENDING = pick("Нужен перезапуск", "Потрібен перезапуск", "Restart needed");
    public static final String RESTART = pick("Перезапустить", "Перезапустити", "Restart");
    public static final String HIDDEN = pick("Анти A/B", "Анти A/B", "Anti A/B");
    public static final String HIDDEN_NOTE = pick("Функции у TikTok уже написаны, но выдаются случайной части людей. " + "Здесь они включаются всем.",
            "Функції в TikTok уже написані, але видаються випадковій частині людей. " + "Тут вони вмикаються всім.", "TikTok has written these already and hands them to a random share of "
            + "people. Here they are switched on for everyone.");
    public static final String CLOSE = pick("Понятно", "Зрозуміло", "Got it");
    public static final String SAVE_STICKER = pick("Скачать стикер", "Завантажити стікер", "Download the sticker");
    public static final String STICKER_FAILED = pick("Не получилось сохранить стикер", "Не вдалося зберегти стікер", "Could not save the sticker");
    public static final String SAVE_AVATARS_ON = pick("Кнопка на аватарках", "Кнопка на аватарках", "The button on avatars");
    public static final String SAVE_STICKERS_ON = pick("Кнопка на стикерах", "Кнопка на стікерах", "The button on stickers");
    public static final String COMMENTS = pick("Комментарии", "Коментарі", "Comments");
    public static final String COMMENT_COPY_ON = pick("Копирование комментариев", "Копіювання коментарів", "Copy comments");
    public static final String COMMENT_COPY = pick("Скопировать комментарий", "Скопіювати коментар", "Copy comment");
    public static final String COMMENT_SELECT = pick("Выбрать фрагмент", "Вибрати фрагмент", "Select part");
    public static final String COMMENT_SELECT_COPY = pick("Выделить и копировать", "Виділити й копіювати", "Select and copy");
    public static final String COMMENT_TIKTOK = pick("Действия TikTok", "Дії TikTok", "TikTok actions");
    public static final String COMMENT_COPIED = pick("Комментарий скопирован", "Коментар скопійовано", "Comment copied");
    public static final String BADGES_OF_ACCOUNT = pick("Все значки аккаунта", "Усі значки акаунта", "All badges of this account");
    public static final String BADGES_ON = pick("Значки", "Значки", "Badges");
    public static final String SAVE_AVATAR = pick("Сохранить аватарку", "Зберегти аватарку", "Save the avatar");
    public static final String SAVED = pick("Сохранено", "Збережено", "Saved");
    public static final String AVATAR_NOTHING = pick("Нечего сохранять — откройте аватарку сначала", "Нема чого зберігати — відкрийте аватарку спершу",
            "Nothing to save yet -- open an avatar first");
    public static final String AVATAR_FAILED = pick("Не получилось сохранить", "Не вдалося зберегти", "Could not save it");
    public static final String BADGE_OWNER = pick("Автор исходного проекта", "Автор вихідного проєкту", "Author of the original project");
    public static final String VIDEO = pick("Видео", "Відео", "Video");
    public static final String THEME = pick("Тема", "Тема", "Theme");
    public static final String LOADING_SHAPES = pick("Загрузка фигурами", "Завантаження фігурами", "Shapes while loading");
    public static final String LOADING_NOTE = pick("Вместо двух кружков TikTok крутятся фигуры Material 3 в цвете акцента.",
            "Замість двох кружечків TikTok крутяться фігури Material 3 у кольорі акценту.", "Material 3 shapes in the accent colour, in place of TikTok's two circles.");
    public static final String ROUND_CARDS = pick("Скруглённые карточки видео", "Заокруглені картки відео", "Rounded video cards");
    public static final String THEME_MATERIAL = pick("Цвета своего телефона", "Кольори свого телефона", "Your phone's own colours");
    public static final String THEME_OLD = pick("Нужен Android 12 или новее: раньше у телефона нет своей палитры.", "Потрібен Android 12 або новіший: раніше телефон не має своєї палітри.",
            "Needs Android 12 or newer: before that a phone has no palette of its own.");
    public static final String THEME_NOTE = pick("Перекрашивается только то, что TikTok и сам перекрашивает при " + "смене светлой темы на тёмную. Акцент живёт отдельно.",
            "Перефарбовується лише те, що TikTok і сам перефарбовує при зміні " + "світлої теми на темну. Акцент живе окремо.",
            "Only what TikTok itself repaints when you switch between light and " + "dark. The accent is its own thing.");
    public static final String BACKGROUND = pick("Играть в фоне", "Грати у фоні", "Play in the background");
    public static final String AUTOSCROLL = pick("Автопрокрутка ленты", "Автопрокрутка стрічки", "Scroll the feed by itself");
    public static final String SOUND = pick("Звук, снятый по копирайту", "Звук, знятий за копірайтом", "Sound pulled for copyright");
    public static final String SEEKBAR = pick("Перемотка на всех видео", "Перемотка на всіх відео", "The scrubbing bar everywhere");
    public static final String VOICE = pick("Голосовые комментарии", "Голосові коментарі", "Voice comments");
    public static final String ACCENT_WALLPAPER = pick("Цвет своего телефона", "Колір свого телефона", "Your phone's own colour");
    public static final String FONT = pick("Шрифт", "Шрифт", "Typeface");
    public static final String TEST_CLOSE = pick("Понятно", "Зрозуміло", "I see");
    public static final String OWN_BRIGHTNESS = pick("TikTok не меняет яркость", "TikTok не змінює яскравість", "TikTok leaves the brightness alone");
    public static final String OWN_BRIGHTNESS_NOTE = pick("TikTok сам выкручивает яркость на максимум в камере и на HDR-видео. "
            + "С этой настройкой она остаётся такой, какую выставил ты. " + "HDR-видео играют как обычные.", "TikTok сам викручує яскравість на максимум у камері й на HDR-відео. "
            + "Із цим налаштуванням вона лишається такою, яку виставив ти. " + "HDR-відео грають як звичайні.", "TikTok turns the brightness all the way up in the camera and on HDR videos. "
            + "With this on it stays where you set it. HDR videos play as ordinary ones.");
    public static final String GRADIENT = pick("Градиент на ник", "Градієнт на нік", "Name gradient");
    public static final String GRADIENT_NOTE = pick("Ник в градиенте видят все, у кого стоит soufiT. Менять можно раз в минуту.",
            "Нік у градієнті бачать усі, у кого стоїть soufiT. Міняти можна раз на хвилину.", "Everyone running soufiT sees it. Changing is allowed once a minute.");
    public static final String GRADIENT_ONLY = pick("Только для поддержавших разработку", "Лише для тих, хто підтримав розробку",
            "For the people who supported original project - MargyT");
    public static final String GRADIENT_HUE = pick("Цвет", "Колір", "Hue");
    public static final String GRADIENT_SAT = pick("Насыщенность", "Насиченість", "Saturation");
    public static final String GRADIENT_VALUE = pick("Яркость", "Яскравість", "Brightness");
    public static final String GRADIENT_ADD = pick("Добавить цвет", "Додати колір", "Add a colour");
    public static final String GRADIENT_DROP_ONE = pick("Убрать цвет", "Прибрати колір", "Remove a colour");
    public static final String GRADIENT_SAVE = pick("Сохранить градиент", "Зберегти градієнт", "Save it");
    public static final String GRADIENT_OFF = pick("Убрать градиент", "Прибрати градієнт", "Take it off");
    public static final String GRADIENT_SAVED = pick("Готово", "Готово", "Done");
    public static final String GRADIENT_REFUSED = pick("Сервер отказал. Раз в минуту, и только для поддержавших.", "Сервер відмовив. Раз на хвилину, і лише для тих, хто підтримав.",
            "The server said no. Once a minute, and supporters only.");
    public static final String GRADIENT_NO_ACCOUNT = pick("Сначала войди в аккаунт", "Спочатку увійди в акаунт", "Sign in first");
    public static final String PROVE = pick("Подтвердить аккаунт", "Підтвердити акаунт", "Prove the account is yours");
    public static final String PROVE_NOTE = pick("Без этого сервер не даст менять значки и градиент.", "Без цього сервер не дасть змінювати значки й градієнт.",
            "Without it the server will not let anything be changed.");
    public static final String PROVE_DONE = pick("Аккаунт подтверждён", "Акаунт підтверджено", "Account proved");
    public static final String PROVE_DONE_NOTE = pick("Код можно убрать из описания профиля.", "Код можна прибрати з опису профілю.",
            "The code can come out of the bio now.");
    public static final String PROVE_HOW = pick("1. Скопируй код.\n" + "2. Вставь его в описание профиля в TikTok и сохрани.\n"
            + "3. Вернись сюда и нажми «Проверить».\n" + "Потом код можно убрать из описания.", "1. Скопіюй код.\n" + "2. Встав його в опис профілю в TikTok і збережи.\n"
            + "3. Повернись сюди і натисни «Перевірити».\n" + "Потім код можна прибрати з опису.", "1. Copy the code.\n" + "2. Put it in your TikTok bio and save.\n"
            + "3. Come back here and press Check.\n" + "The code can come out afterwards.");
    public static final String PROVE_WHY = pick("Так сервер убеждается, что аккаунт твой: в чужое описание никто " + "написать не может.",
            "Так сервер переконується, що акаунт твій: у чужий опис ніхто " + "написати не може.", "It is the one thing only the owner of an account can do.");
    public static final String PROVE_COPY = pick("Код скопирован", "Код скопійовано", "Code copied");
    public static final String PROVE_TAP = pick("Нажми на код, чтобы скопировать", "Натисни на код, щоб скопіювати", "Tap the code to copy it");
    public static final String PROVE_FRESH = pick("Другой код", "Інший код", "A different code");
    public static final String PROVE_FRESH_DONE = pick("Код заменён, вставь новый в описание", "Код замінено, встав новий в опис",
            "Here is another one, put it in the bio");
    public static final String PROVE_CHECKING = pick("Смотрю твою страницу…", "Дивлюсь твою сторінку…", "Reading your page...");
    public static final String PROVE_NEED_NAME = pick("Не могу найти твою страницу по айди. Впиши свой ник и жми ещё раз.",
            "Не можу знайти твою сторінку за айді. Впиши свій нік і тисни ще раз.", "Your page cannot be found from the id. Write your name and press again.");
    public static final String PROVE_NAME = pick("твой ник без @", "твій нік без @", "your name without the @");
    public static final String PROVE_NOT_YOURS = pick("Этот код просили не с этого телефона. Открой карточку заново.", "Цей код просили не з цього телефона. Відкрий картку заново.",
            "This code was asked for by another phone. Open the card again.");
    public static final String PROVE_HIDDEN = pick("Если TikTok прячет описание от других, впиши код в имя профиля: " + "оно обновляется сразу.",
            "Якщо TikTok ховає опис від інших, впиши код в ім'я профілю: " + "воно оновлюється одразу.", "If TikTok is holding your bio back, put the code in your profile "
            + "name instead: that shows up at once.");
    public static final String PROVE_CHECK = pick("Проверить", "Перевірити", "Check");
    public static final String PROVE_OK = pick("Готово, аккаунт твой", "Готово, акаунт твій", "Done, it is yours");
    public static final String PROVE_NOT_THERE = pick("Кода в описании не видно. Сохрани описание и жми ещё раз, " + "TikTok обновляет страницу не сразу.",
            "Коду в описі не видно. Збережи опис і тисни ще раз, " + "TikTok оновлює сторінку не одразу.", "The code is not on the page yet. Save the bio and press again, "
            + "TikTok takes a moment to show it.");
    public static final String PROVE_OTHER_NAME = pick("Этот ник принадлежит другому аккаунту", "Цей нік належить іншому акаунту",
            "That name belongs to another account");
    public static final String PROVE_NO_TIKTOK = pick("TikTok не ответил, попробуй ещё раз", "TikTok не відповів, спробуй ще раз",
            "TikTok did not answer, try again");
    public static final String PROVE_NO_SERVER = pick("Сервер не отвечает", "Сервер не відповідає", "The server is not answering");
    public static final String PROVE_STALE = pick("Код устарел. Вот новый, вставь его.", "Код застарів. Ось новий, встав його.",
            "That code ran out. Here is a new one.");
    public static final String PROVE_TOO_OFTEN = pick("Слишком часто, подожди немного", "Занадто часто, зачекай трохи", "Too often, wait a moment");
    public static final String PROVE_NO_ACCOUNT = pick("Сначала войди в аккаунт", "Спочатку увійди в акаунт", "Sign in first");
    public static final String PROVE_FAILED = pick("Не вышло", "Не вийшло", "That did not work");
    public static final String MADE_BY = pick("Форк от @soufied · MargyT от @narezany\nGPL-3.0 с доп. условиями", "Форк від @soufied · MargyT від @narezany\nGPL-3.0 з дод. умовами",
            "Fork by @soufied · MargyT by @narezany\nGPL-3.0 with additional terms");
    public static final String TAGS = pick("Фильтр хештегов", "Фільтр хештегів", "Hashtag filter");
    public static final String TAGS_NOTE = pick("Посты с этими хештегами не попадут в рекомендации. Свои посты не прячутся.",
            "Пости з цими хештегами не потраплять у рекомендації. Свої пости не ховаються.", "Posts with these hashtags stay out of the feed. Your own are never hidden.");
    public static final String TAGS_ADD = pick("Добавить хештег", "Додати хештег", "Add a hashtag");
    public static final String TAGS_NONE = pick("Пока пусто", "Поки порожньо", "None yet");
    public static final String TAGS_FULL = pick("Больше не влезет", "Більше не влізе", "That is enough of those");
    public static final String FPS = pick("Частота кадров", "Частота кадрів", "Frame rate");
    public static final String FPS_ABOUT = pick("TikTok сам решает, сколько кадров показывать: где-то 60, где-то 120.", "TikTok сам вирішує, скільки кадрів показувати: десь 60, десь 120.",
            "TikTok decides how many frames to show: 60 here, 120 there.");
    public static final String FPS_AUTO = pick("Пусть решает TikTok", "Хай вирішує TikTok", "Let TikTok decide");
    public static final String FPS_LOCKED = pick("Всегда %s", "Завжди %s", "Always %s");
    public static final String FPS_UNSUPPORTED = pick("Экран столько не умеет — будет ближайшая", "Екран стільки не вміє — буде найближча",
            "The screen cannot do that, so the nearest one instead");
    public static final String FONT_SYSTEM = pick("Системный", "Системний", "The system one");
    public static final String FONT_SANS = pick("Обычный", "Звичайний", "Sans");
    public static final String FONT_SANS_LIGHT = pick("Тонкий", "Тонкий", "Light");
    public static final String FONT_SANS_CONDENSED = pick("Узкий", "Вузький", "Condensed");
    public static final String FONT_SERIF = pick("С засечками", "Із засічками", "Serif");
    public static final String FONT_MONOSPACE = pick("Моноширинный", "Моноширинний", "Monospace");
    public static final String FONT_CURSIVE = pick("Рукописный", "Рукописний", "Cursive");
    public static final String FONT_FILE = pick("Свой файл", "Свій файл", "A file of your own");
    public static final String FONT_PICK = pick("Выбрать .ttf или .otf", "Обрати .ttf або .otf", "Pick a .ttf or .otf");
    public static final String FONT_FAILED = pick("Не получилось прочитать шрифт", "Не вдалося прочитати шрифт", "That file is not a font this phone can read");
    public static final String EMOJI = pick("Шрифт эмодзи", "Шрифт емодзі", "Emoji");
    public static final String EMOJI_SYSTEM = pick("Системные", "Системні", "The system ones");
    public static final String EMOJI_TWEMOJI = pick("Twemoji", "Twemoji", "Twemoji");
    public static final String EMOJI_NOTO = pick("Noto", "Noto", "Noto");
    public static final String EMOJI_BLOB = pick("Blobmoji", "Blobmoji", "Blobmoji");
    public static final String EMOJI_FILE = pick("Свой файл", "Свій файл", "A file of your own");
    public static final String EMOJI_PREVIEW = "😀  🐱  🫶  👩🏽‍💻  🇷🇺  1️⃣";
    public static final String EMOJI_NOTE = pick("Все паки уже внутри мода, скачивать ничего не нужно. Нужен Android 10 "
            + "и выше: ниже него меняются только буквы.", "Усі паки вже всередині мода, завантажувати нічого не треба. Потрібен "
            + "Android 10 і вище: нижче змінюються лише літери.", "Every pack is already inside the mod; nothing is fetched. Android 10 "
            + "and up: below that only the letters change.");
    public static final String EMOJI_READY = pick("Готово — перезапусти приложение", "Готово — перезапусти застосунок", "Done -- restart the app");
    public static final String ICON = pick("Иконка", "Іконка", "The icon");
    public static final String ICON_NOTE = pick("Иконки с конкурса. Хочешь, чтобы твоя была здесь — участвуй.", "Іконки з конкурсу. Хочеш, щоб твоя була тут — бери участь.",
            "These came from a contest. Enter it if you want yours here.");
    public static final String ICON_CONTEST = pick("Участвовать в конкурсе", "Взяти участь у конкурсі", "Enter the contest");
    public static final String ICON_DEFAULT = pick("Обычная", "Звичайна", "The usual one");
    public static final String DONATE_BANNER = pick("Значок за поддержку оригинального проекта — MargyT", "Значок за підтримку оригінального проєкту — MargyT",
            "A badge for supporting original project - MargyT");
    public static final String DONATE_BANNER_TEXT = pick("От 250 ₽: значок рядом с ником, градиент на ник и закрытый чат. "
            + "Значок и градиент видят все с MargyT/soufiT.", "Від 250 ₽: значок поруч із ніком, градієнт на нік і закритий чат. "
            + "Значок і градієнт бачать усі з MargyT/soufiT.", "From 250 ₽: a badge beside your name, a gradient on the name "
            + "and the private chat. The badge and the gradient are seen " + "by everyone on MargyT/soufiT.");
    public static final String DONATE_BANNER_HOW = pick("После перевода — в сообщения канала: чек и ваш ID аккаунта.", "Після переказу — у повідомлення каналу: чек і ваш ID акаунта.",
            "After paying, write to the channel with the receipt and your " + "account ID.");
    public static final String DONATE_BANNER_BUTTON = pick("Пожертвовать", "Пожертвувати", "Donate");
    public static final String DONATE_BANNER_WRITE = pick("Сообщения каналу", "Повідомлення каналу", "Message the channel");
    public static final String DONATE_STEP_PAY = pick("1. Переведите от 250 ₽", "1. Переказати від 250 ₽", "1. Send 250 ₽ or more");
    public static final String DONATE_STEP_ID = pick("2. Скопируйте свой ID", "2. Скопіюйте свій ID", "2. Copy your ID");
    public static final String DONATE_ID_NOTE = pick("Нажмите на ID, он скопируется. По нему выдаётся значок.", "Натисніть на ID, він скопіюється. За ним видається значок.",
            "Tap the ID to copy it. The badge is given to this account.");
    public static final String DONATE_STEP_WRITE = pick("3. Напишите в канал", "3. Напишіть у канал", "3. Write to the channel");
    public static final String DONATE_WRITE_NOTE = pick("Пришлите чек и ID", "Надішліть чек і ID", "Send the receipt and the ID");
    public static final String DONATE_AFTER = pick("Значок выдаётся вручную, обычно в течение дня. Градиент на ник и " + "ссылка в закрытый чат придут вместе с ним.",
            "Значок видається вручну, зазвичай протягом дня. Градієнт на нік і " + "посилання в закритий чат прийдуть разом із ним.",
            "The badge is given by hand, usually within a day. The gradient on " + "your name and the link to the closed chat come with it.");
    public static final String TIKTOK_FEATURES = pick("Функции TikTok", "Функції TikTok", "TikTok features");
    public static final String TIKTOK_FEATURES_NOTE = pick("Это функции самого TikTok, которые он пока включает не всем. Здесь их "
            + "можно включить себе. Что-то может работать не до конца.", "Це функції самого TikTok, які він поки вмикає не всім. Тут їх можна "
            + "увімкнути собі. Щось може працювати не до кінця.", "These are TikTok's own features that it has not switched on for everybody yet. "
            + "Here you can switch them on for yourself. Some may not fully work.");
    public static final String COMMENT_REPOST = pick("Репост комментариев", "Репост коментарів", "Repost comments");
    public static final String SELF_REPOST = pick("Репост своих видео", "Репост своїх відео", "Repost your own videos");
    public static final String TRANSLATE_COMMENTS = pick("Перевод всех комментариев сразу", "Переклад усіх коментарів одразу",
            "Translate all comments at once");
    public static final String AUTO_HIDE_BAR = pick("Прятать нижнюю панель при затемнении", "Ховати нижню панель при затемненні",
            "Hide the bottom bar when dimmed");
    public static final String LIQUID_GLASS = pick("Дизайн", "Дизайн", "Design");
    public static final String GLASS_BAR = pick("Нижняя панель", "Нижня панель", "Bottom bar");
    public static final String GLASS_PROFILE = pick("Профиль", "Профіль", "Profile");
    public static final String GLASS_MD3 = pick("Material 3", "Material 3", "Material 3");
    public static final String GLASS_DEFAULT = pick("Стандартно", "Стандартно", "Default");
    public static final String GLASS_ON = pick("Стекло", "Скло", "Glass");
    public static final String GLASS_NOTE = pick("Стекло — полупрозрачный слой с подсветкой по краю. Нижняя панель и профиль выбираются отдельно.",
            "Скло — напівпрозрачний шар із підсвіткою по краю. Нижня панель і профіль обираються окремо.", "Glass is a see-through layer with a lit edge. The bottom bar and the profile are chosen apart.");
    public static final String NEW_PROFILE = pick("Новый вид профиля TikTok", "Новий вигляд профілю TikTok", "TikTok's new profile look");
    public static final String NAV_CAPSULE = pick("Нижняя панель в стиле Material 3", "Нижня панель у стилі Material 3", "Material 3 bottom bar");
    public static final String MD3_FEED = pick("Лента в стиле Material 3", "Стрічка у стилі Material 3", "Material 3 feed");
    public static final String MD3_PROFILE = pick("Профиль в стиле Material 3", "Профіль у стилі Material 3", "Material 3 profile");
    public static final String MD3_ICONS = pick("Округлые иконки у кнопок видео", "Округлі іконки біля кнопок відео", "Rounded icons on a video's buttons");
    public static final String MD3_INBOX = pick("Входящие в стиле Material 3", "Вхідні у стилі Material 3", "Material 3 inbox");
    public static final String TAB_READ_ALL = pick("Прочитать все?", "Прочитати все?", "Mark all as read?");
    public static final String TAB_READ_ALL_NOTE = pick("Число на кнопке пропадёт и вернётся, когда придёт что-то новое.",
            "Число на кнопці зникне й повернеться, коли надійде щось нове.", "The number on the tab goes away and comes back when something new arrives.");
    public static final String TAB_READ_ALL_YES = pick("Прочитать", "Прочитати", "Mark as read");
    public static final String TAB_UNREAD_BACK = pick("Вернуть число?", "Повернути число?", "Bring the number back?");
    public static final String TAB_UNREAD_BACK_NOTE = pick("Сейчас число на этой кнопке скрыто.", "Зараз число на цій кнопці приховано.",
            "The number on this tab is hidden at the moment.");
    public static final String TAB_UNREAD_BACK_YES = pick("Вернуть", "Повернути", "Bring it back");
    public static final String AVATAR_SHAPE = pick("Форма аватарок", "Форма аватарок", "Avatar shape");
    public static final String SHAPE_CIRCLE = pick("Круг", "Коло", "Circle");
    public static final String SHAPE_ROUNDED = pick("Скруглённый", "Заокруглений", "Rounded");
    public static final String SHAPE_SQUARE = pick("Квадрат", "Квадрат", "Square");
    public static final String SHAPE_COOKIE = pick("Печенье", "Печиво", "Cookie");
    public static final String SHAPE_CLOVER = pick("Клевер", "Конюшина", "Clover");
    public static final String SHAPE_BURST = pick("Вспышка", "Спалах", "Burst");
    public static final String SHAPE_SUN = pick("Солнце", "Сонце", "Sun");
    public static final String SHAPE_PENTAGON = pick("Пятиугольник", "П'ятикутник", "Pentagon");
    public static final String SHAPE_HEXAGON = pick("Шестиугольник", "Шестикутник", "Hexagon");
    public static final String SHAPE_HEART = pick("Сердце", "Серце", "Heart");
    public static final String SCREAMER_SOUND = pick("Звук скримера", "Звук скримера", "Screamer sound");
    public static final String FUN = pick("Смешнявки", "Смішнявки", "Silly things");
    public static final String FUN_NOTE = pick("Деп калькулятор, скримеры и тилкайо", "Деп калькулятор, скримери й тілкайо",
            "Dep Calculator, screamers and tilcayo");
    public static final String FUN_HELP = pick("Здесь то, что ни для чего не нужно. Скримеры выключены, пока вы сами их не включите.",
            "Тут те, що ні для чого не потрібне. Скримери вимкнені, доки ви самі їх не ввімкнете.", "Things that are for nothing at all. Screamers stay off until you switch them on yourself.");
    public static final String FUN_SCREAMERS = pick("Скримеры", "Скримери", "Screamers");
    public static final String SCREAMERS_NOTE = pick("Время от времени на весь экран выскакивает растянутый жирный тилкайо и орёт. "
            + "Громко ровно настолько, насколько сейчас включён звук.", "Час від часу на весь екран вискакує розтягнутий жирний тілкайо й репетує. "
            + "Гучно рівно настільки, наскільки зараз увімкнено звук.", "Every so often a stretched fat tilcayo fills the screen and yells. "
            + "As loud as your media volume is, and no louder.");
    public static final String SCREAMER_NOW = pick("Показать сейчас", "Показати зараз", "Show one now");
    public static final String SHOWCASE = pick("Витрина", "Вітрина", "Showcase");
    public static final String SHOWCASE_NOTE = pick("Примерьте любой значок и градиент на свой ник. Ваши собственные отмечены «Ваш», остальные — просмотр: другим они не видны.",
            "Приміряйте будь-який значок і градієнт на свій нік. Ваші власні позначені «Ваш», решта — перегляд: іншим вони не видно.",
            "Try any badge and gradient on your name. The ones marked Yours are yours; the rest is a preview only, and others never see it.");
    public static final String SHOWCASE_NAME = pick("Ваш ник", "Ваш нік", "Your nickname");
    public static final String SHOWCASE_NAME_NOTE = pick("без значка", "без значка", "no badge");
    public static final String SHOWCASE_PREVIEW_NOTE = pick("просмотр — не ваш", "перегляд — не ваш", "preview — not yours");
    public static final String SHOWCASE_BADGES = pick("Значки", "Значки", "Badges");
    public static final String SHOWCASE_GRADIENTS = pick("Градиенты", "Градієнти", "Gradients");
    public static final String FULL_AMOLED = pick("Full AMOLED: весь интерфейс", "Full AMOLED: увесь інтерфейс", "Full AMOLED: whole interface");
    public static final String FULL_AMOLED_NOTE = pick("Чистый чёрный на всём: фон TikTok, карточки, нижняя панель и Liquid Glass. Включает AMOLED.",
            "Чистий чорний на всьому: фон TikTok, картки, нижня панель і Liquid Glass. Вмикає AMOLED.", "Pure black everywhere: TikTok's background, cards, the bottom bar and Liquid Glass. Turns AMOLED on.");
    public static final String SUGGESTED_ACCOUNTS = pick("Рекомендуемые аккаунты", "Рекомендовані акаунти", "Suggested accounts");
    public static final String HIDE_SUGGESTED_ACCOUNTS = pick("Скрывать рекомендуемые аккаунты", "Приховувати рекомендовані акаунти",
            "Hide suggested accounts");
    public static final String HIDE_SUGGESTED_ACCOUNTS_NOTE = pick("Прячет блок «Рекомендуемые аккаунты» под видео и на странице поиска друзей.",
            "Ховає блок «Рекомендовані акаунти» під відео та на сторінці пошуку друзів.", "Hides the Suggested accounts strip under videos and on the Find friends page.");
    public static final String SHOW_REGION_LANG = pick("Регион и язык", "Регіон і мова", "Region and language");
    public static final String STATUS_HIDDEN = pick("Скрыть статус-бар", "Приховати статус-бар", "Hide status bar");
    public static final String BLOCK_SITES = pick("Сайты", "Сайти", "Sites");
    public static final String BLOCK_YZEWE = pick("Блокировать yzewe.ru", "Блокувати yzewe.ru", "Block yzewe.ru");
    public static final String BLOCK_YZEWE_NOTE = pick("Мод не открывает и не запрашивает этот сайт и его поддомены. Ссылки внутри самого TikTok мод пока не перехватывает.",
            "Мод не відкриває і не запитує цей сайт та його піддомени. Посилання всередині самого TikTok мод поки не перехоплює.",
            "The mod won't open or fetch this site or its subdomains. Links inside TikTok itself are not intercepted yet.");
    public static final String SITE_BLOCKED = pick("Сайт заблокирован", "Сайт заблоковано", "That site is blocked");
    public static final String SHOWCASE_NONE = pick("Нет", "Немає", "None");
    public static final String SHOWCASE_NO_BADGE = pick("Без значка", "Без значка", "No badge");
    public static final String SHOWCASE_NO_GRADIENT = pick("Без градиента", "Без градієнта", "No gradient");
    public static final String PERKS = pick("Поддержка оригинального проекта", "Підтримка оригінального проєкту", "Original Project Support");
    public static final String PERKS_NOTE = pick("Градиент на ник и значки, которые видите вы. Остальные видят то, что у вас есть.",
            "Градієнт на нік і значки, які бачите ви. Інші бачать те, що у вас є.", "The gradient on your name and the badges you hold. Others see what you own.");
    public static final String SHOWCASE_OWNED = pick("Ваш", "Ваш", "Yours");
    public static final String SHOWCASE_PREVIEW = pick("Просмотр", "Перегляд", "Preview");
    public static final String SHOWCASE_NO_BADGES = pick("Список пока не загружен. Нажмите «Загрузить значки» ниже.", "Список ще не завантажено. Натисніть «Завантажити значки» нижче.",
            "The list is not loaded yet. Tap “Load badges” below.");
    public static final String BADGES_LOAD = pick("Загрузить значки", "Завантажити значки", "Load badges");
    public static final String BADGES_LOAD_NOTE = pick("Сама ничего не загружается. Нажмите, чтобы взять список с сервера.",
            "Сама нічого не завантажується. Натисніть, щоб взяти список із сервера.", "Nothing loads by itself. Tap to fetch the list from the server.");
    public static final String BADGES_LOADED = pick("Значки обновлены", "Значки оновлено", "Badges updated");
    public static final String BADGES_UNREACHED = pick("Сервер не ответил. Проверьте адрес и интернет.", "Сервер не відповів. Перевірте адресу й інтернет.",
            "The server did not answer. Check the address and the connection.");
    public static final String BIO_RESTORE = pick("Вернуть прежнее био", "Повернути попереднє біо", "Put my old bio back");
    public static final String BIO_RESTORE_NOTE = pick("Нужно, только если автоматическая проверка прервалась: в описании профиля остался код.",
            "Потрібно, лише якщо автоматичну перевірку перервано: в описі профілю лишився код.", "Only needed if the automatic check was cut short and its code is still in your bio.");
    public static final String BIO_RESTORE_GOING = pick("Возвращаю прежнее био", "Повертаю попереднє біо", "Putting your old bio back");
    public static final String SHELF_LOOK = pick("Иконка, акцент, тема, шрифты и формы", "Іконка, акцент, тема, шрифти й форми",
            "Icon, accent, theme, fonts and shapes");
    public static final String SHELF_PROFILE = pick("Витрина, значки, градиент и аккаунт", "Вітрина, значки, градієнт і акаунт",
            "Showcase, badges, gradient and account");
    public static final String SHELF_FEED = pick("Лента, видео, кнопки, комментарии и регион", "Стрічка, відео, кнопки, коментарі та регіон",
            "Feed, video, buttons, comments and region");
    public static final String SHELF_TOOLS = pick("Скачивание, аватары, стикеры и копии", "Завантаження, аватари, стікери та копії",
            "Downloads, avatars, stickers and backups");
    public static final String SHELF_ABOUT = pick("Версия, ссылки, канал и благодарности", "Версія, посилання, канал і подяки",
            "Version, links, channel and thanks");
    public static final String SHELF_PLUGINS = pick("Установка плагинов и их настройки", "Встановлення плагінів і їхні налаштування",
            "Installing plugins and their settings");
    public static final String SHELF_FRIENDS = pick("Закреплённые друзья, серии и уведомления", "Закріплені друзі, серії та сповіщення",
            "Pinned friends, streaks and notifications");
    public static final String HELP_LOOK = pick("Иконка приложения, акцент (цвет кнопок и вкладок), формы загрузки, скругление карточек, форма аватара, капсула навигации, Material 3 для ленты, значков и профиля, тема и шрифты. Готовые картинки могут сохранять свой цвет.",
            "Іконка застосунку, акцент (колір кнопок і вкладок), форми завантаження, заокруглення карток, форма аватара, капсула навігації, Material 3 для стрічки, значків і профілю, тема та шрифти. Готові картинки можуть зберігати свій колір.",
            "The app icon, the accent (the colour of buttons and tabs), loading shapes, round cards, the avatar shape, the navigation capsule, Material 3 for the feed, icons and profile, the theme and the fonts. Ready-made pictures may keep their own colour.");
    public static final String HELP_PROFILE = pick("Баннер поддержки, витрина с выбранными значком и градиентом (только для витрины), значки на ник и их порядок, градиент на ник и аккаунт. Чтобы менять значки и градиент, аккаунт подтверждается один раз. Адрес сервера значков виден только в режиме разработчика.",
            "Банер підтримки, вітрина з обраними значком і градієнтом (лише для вітрини), значки на нік і їхній порядок, градієнт на нік та акаунт. Щоб змінювати значки та градієнт, акаунт підтверджується один раз. Адреса сервера значків видна лише в режимі розробника.",
            "The support banner, the showcase with a badge and gradient you pick (for the showcase only), the badges on your name and their order, the gradient on your name, and the account. To change badges or the gradient, the account is confirmed once. The badge server address shows only in developer mode.");
    public static final String HELP_FEED = pick("Лента: что скрывать (реклама, лайвы, фото, истории, предложения), вход и дата рождения. Видео: скорость, звук, перемотка, дата и яркость. Кнопки в ленте, защита экрана (глаз на видео), комментарии, функции TikTok, FPS, регион и теги.",
            "Стрічка: що приховувати (реклама, лайви, фото, історії, пропозиції), вхід і дата народження. Відео: швидкість, звук, перемотування, дата й яскравість. Кнопки в стрічці, захист екрана (око на відео), коментарі, функції TikTok, FPS, регіон і теги.",
            "Feed: what to hide (ads, lives, photos, stories, suggestions), sign-in and date of birth. Video: speed, sound, seeking, the date and brightness. Feed buttons, screen protection (the eye on a video), comments, TikTok features, FPS, region and tags.");
    public static final String HELP_TOOLS = pick("Качество кадра, водяной знак, скачивание без водяного знака, аватары и стикеры, а также резервные копии. Сохраните копию перед экспериментами.",
            "Якість кадру, водяний знак, завантаження без водяного знака, аватари та стікери, а також резервні копії. Збережіть копію перед експериментами.",
            "Frame quality, the watermark, downloading without it, avatars and stickers, and backups. Keep a copy before experimenting.");
    public static final String HELP_ABOUT = pick("Версия мода, обновления, ссылки на исходный код и каналы, благодарности тем, кто помогал.",
            "Версія мода, оновлення, посилання на вихідний код і канали, подяки тим, хто допомагав.", "The mod's version, updates, links to the source and the channels, and thanks to the people who helped.");
    public static final String HELP_PLUGINS = pick("Магазин, установка и настройки плагинов. Плагины запускают дополнительный код; устанавливайте только те, которым доверяете.",
            "Магазин, встановлення й налаштування плагінів. Плагіни запускають додатковий код; встановлюйте лише ті, яким довіряєте.",
            "The store, installing and setting up plugins. Plugins run extra code; install only the ones you trust.");
    public static final String HELP_FRIENDS = pick("Закреплённые друзья, автопродление серий (расписание и стикеры) и уведомления мода: сообщения, промо и остальные, а также запрос разрешения Android.",
            "Закріплені друзі, автопродовження серій (розклад і стікери) та сповіщення мода: повідомлення, промо й інші, а також запит дозволу Android.",
            "Pinned friends, keeping streaks alive (the schedule and stickers), and the mod's notifications: messages, promotions and the others, plus the Android permission.");
    public static final String DEVELOPER_MENU = pick("Меню разработчика", "Меню розробника", "Developer menu");
    public static final String DEVELOPER_NOTE = pick("Журнал, диагностика, флаги и отчёт", "Журнал, діагностика, прапорці та звіт",
            "Diary, diagnostics, flags and report");
    public static final String DEVELOPER_HELP = pick("Журнал, диагностика и копии для разработки. Появляется, когда включён режим разработчика: пять нажатий на «soufiT».",
            "Журнал, діагностика та копії для розробки. З’являється, коли увімкнено режим розробника: п’ять натискань на «soufiT».",
            "The diary, diagnostics and copies for development. Shown when developer mode is on: five taps on «soufiT».");
    public static final String FORK_OF = pick("Форк MargyT", "Форк MargyT", "Fork of MargyT");
    public static final String COUNTRY_SEARCH = pick("Поиск страны", "Пошук країни", "Search country");
    public static final String FUN_CAT = pick("Жирный тилкайо", "Жирний тілкайо", "Fat tilcayo");
    public static final String DEV_FLAGS = pick("Флаги TikTok", "Прапорці TikTok", "TikTok flags");
    public static final String DEV_FLAGS_NOTE = pick("Все флаги и их значения", "Усі прапорці та їхні значення", "Every flag and its value");
    public static final String FLAGS_WARNING = pick("Экспериментально. Флаги управляют внутренностями TikTok: от изменения что-то "
            + "может сломаться, вылетать или не измениться вообще. Если TikTok три раза " + "подряд не сможет запуститься, ваши изменения отключатся сами.",
            "Експериментально. Прапорці керують нутрощами TikTok: від зміни щось може " + "зламатися, вилітати або не змінитися взагалі. Якщо TikTok тричі поспіль "
            + "не зможе запуститися, ваші зміни вимкнуться самі.", "Experimental. Flags steer the insides of TikTok: changing one can break something, "
            + "crash the app, or do nothing at all. If TikTok fails to start three times " + "in a row, your changes switch themselves off.");
    public static final String FLAGS_HELD = pick("Ваши изменения отключены: TikTok три раза подряд не смог с ними запуститься.",
            "Ваші зміни вимкнено: TikTok тричі поспіль не зміг із ними запуститися.", "Your changes are switched off: TikTok failed to start with them three times in a row.");
    public static final String FLAGS_AGAIN = pick("Включить снова", "Увімкнути знову", "Switch them back on");
    public static final String FLAGS_SEARCH = pick("Часть названия флага", "Частина назви прапорця", "Part of a flag's name");
    public static final String FLAGS_LOADING = pick("Читаю список флагов…", "Читаю список прапорців…", "Reading the list of flags…");
    public static final String FLAGS_NO_LIST = pick("В этой версии TikTok список флагов не нашёлся.", "У цій версії TikTok список прапорців не знайшовся.",
            "The list of flags was not found in this version of TikTok.");
    public static final String FLAGS_CHANGED = pick("Изменённые", "Змінені", "Changed");
    public static final String FLAGS_NONE_CHANGED = pick("Пока ничего не изменено. Всего флагов: %d. Найдите нужный по названию.",
            "Поки нічого не змінено. Усього прапорців: %d. Знайдіть потрібний за назвою.", "Nothing changed yet. %d flags in all. Find the one you want by name.");
    public static final String FLAGS_FOUND = pick("Найдено: %d", "Знайдено: %d", "Found: %d");
    public static final String FLAGS_MORE = pick("Показаны первые %d. Уточните название.", "Показано перші %d. Уточніть назву.",
            "The first %d are shown. Narrow the name down.");
    public static final String FLAGS_HERE = pick("здесь", "тут", "here");
    public static final String FLAGS_AS_TIKTOK = pick("Как у TikTok", "Як у TikTok", "As TikTok has it");
    public static final String FLAGS_ON = pick("Включить", "Увімкнути", "On");
    public static final String FLAGS_OFF = pick("Выключить", "Вимкнути", "Off");
    public static final String FLAGS_RESET = pick("Сбросить", "Скинути", "Reset");
    public static final String FLAGS_RESET_ALL = pick("Сбросить все изменения", "Скинути всі зміни", "Reset every change");
    public static final String FLAGS_BAD = pick("Это не значение такого типа", "Це не значення такого типу", "That is not a value of this type");
    public static final String FLAGS_FIXED = pick("Флаги такого типа менять нельзя", "Прапорці такого типу змінювати не можна",
            "Flags of this type cannot be changed");
    public static final String SPEED_LOCK = pick("Фиксация скорости свайпом вниз", "Фіксація швидкості свайпом униз", "Lock the speed by swiping down");
    public static final String REMIND_TITLE = pick("Нравится soufiT?", "Подобається soufiT?", "Enjoying soufiT?");
    public static final String REMIND_TEXT = pick("Поддержи разработку — значок рядом с ником, градиент на ник " + "и закрытый чат.",
            "Підтримай розробку — значок поруч із ніком, градієнт на нік " + "і закритий чат.", "Support the making of it: a badge beside your name, a gradient on "
            + "the name and the private chat.");
    public static final String REMIND_MORE = pick("Подробнее", "Докладніше", "Tell me more");
    public static final String REMIND_NEVER = pick("Больше не напоминать", "Більше не нагадувати", "Never remind me");
    public static final String STREAK_TEST = pick("Отправить тестовое сообщение", "Надіслати тестове повідомлення", "Send a test message");
    public static final String STREAK_TEST_NOTE = pick("Всем, у кого есть серия — даже если огонёк горит. Результат в дневнике.",
            "Усім, у кого є серія — навіть якщо вогник горить. Результат у щоденнику.", "To everyone with a streak, lit or not. The diary says what happened.");
    public static final String STREAK_TEST_GOING = pick("Отправляю, смотри дневник", "Надсилаю, дивись щоденник", "Sending; the diary will say");
    public static final String STREAK_TEST_ASK = pick("Тестовое сообщение уйдёт всем, у кого есть серия, — и тем, у кого огонёк уже горит. Это настоящие сообщения.",
            "Тестове повідомлення піде всім, з ким у тебе є серія, — і тим, у кого вогник уже горить. Це справжні повідомлення.",
            "The test message goes to everyone you have a streak with, including lit ones. These are real messages.");
    public static final String STREAK_ALL = pick("Отправить огонёк всем", "Надіслати вогник усім", "Send the flame to everyone");
    public static final String STREAK_ALL_NOTE = pick("Выбранный стикер уйдёт всем, с кем у тебя есть серия, прямо сейчас",
            "Вибраний стікер піде всім, з ким у тебе є серія, просто зараз", "The chosen sticker goes to everyone you have a streak with, right now");
    public static final String STREAK_ALL_ASK = pick("Выбранный стикер будет отправлен всем, с кем у тебя есть серия, — и тем, у кого огонёк уже горит. Это настоящие сообщения.",
            "Вибраний стікер буде надіслано всім, з ким у тебе є серія, — і тим, у кого вогник уже горить. Це справжні повідомлення.",
            "The chosen sticker will be sent to everyone you have a streak with, including lit ones. These are real messages.");
    public static final String STREAK_ALL_SEND = pick("Отправить", "Надіслати", "Send");
    public static final String STREAK_ALL_NO_STICKER = pick("Сначала выбери стикер для серий (текст пока недоступен)", "Спочатку обери стікер для серій (текст поки недоступний)",
            "Choose a streak sticker first (text is not available yet)");
    public static final String STREAK_ALL_NO_CHATS = pick("Сначала открой список чатов TikTok, чтобы мод увидел серии", "Спочатку відкрий список чатів TikTok, щоб мод побачив серії",
            "Open the TikTok chat list first so the mod can see your streaks");
    public static final String STREAK_ALL_NONE = pick("Серий не найдено", "Серій не знайдено", "No streaks found");
    public static final String STREAK_ALL_BUSY = pick("Уже отправляю…", "Уже надсилаю…", "Already sending…");
    public static final String STREAK_ALL_DONE = pick("Отправлено: %1$d из %2$d", "Надіслано: %1$d з %2$d", "Sent: %1$d of %2$d");
    public static final String CAT_FROM = pick("Принёс", "Приніс", "Brought by");
    public static final String CAT_YOURS = pick("Хочешь своего кота здесь? Напиши @narezany в Telegram", "Хочеш свого кота тут? Напиши @narezany в Telegram",
            "Want your cat here? Write to @narezany on Telegram");
    public static final String CAT_WAIT = pick("Коты ещё едут, попробуй ещё раз", "Коти ще їдуть, спробуй ще раз", "The cats are still on their way; try again");
    public static final String HIDE_LIVE = pick("Скрыть трансляции", "Сховати трансляції", "Hide live rooms");
    public static final String HIDE_PHOTOS = pick("Скрыть фото-посты", "Сховати фото-пости", "Hide slideshows");
    public static final String DIM = pick("Анти-выгорание", "Анти-вигоряння", "Stop the screen burning in");
    public static final String DIM_HOW = pick("Насколько приглушить", "Наскільки приглушити", "How far down");
    public static final String DIM_EYE_ON = pick("Прозрачность включена. Нажмите, чтобы выключить", "Прозорість увімкнена. Натисніть, щоб вимкнути",
            "Dimming on. Tap to turn off");
    public static final String DIM_EYE_OFF = pick("Прозрачность выключена. Нажмите, чтобы включить", "Прозорість вимкнена. Натисніть, щоб увімкнути",
            "Dimming off. Tap to turn on");
    public static final String DIM_SHIFT = pick("Плавно сдвигать кнопки", "Плавно зсувати кнопки", "Slowly drift controls");
    public static final String DIM_SHIFT_SIZE = pick("Насколько сдвигать, dp", "Наскільки зсувати, dp", "How far to drift, dp");
    public static final String DIM_NOTE = pick("Приглушение делает панели, кнопки, подпись и индикаторы видео и слайд-шоу тусклее. Фото и видео не меняются. "
            + "Сдвиг медленно водит те же элементы по небольшой области, чтобы " + "износ не копился на одних и тех же пикселях: движение почти "
            + "незаметно, а видео не двигается. Сдвиг работает и без приглушения; " + "нажимаются кнопки так же.", "Приглушення робить панелі, кнопки, підпис та індикатори відео й слайд-шоу тьмянішими. Фото та відео не змінюються. "
            + "Зсув повільно водить ті самі елементи по невеликій ділянці, щоб " + "знос не накопичувався на одних і тих самих пікселях: рух майже "
            + "непомітний, а відео не рухається. Зсув працює і без приглушення; " + "кнопки натискаються так само.", "Dimming makes video and slideshow panels, controls, captions and indicators fainter. Photos and videos stay unchanged. "
            + "Drifting slowly walks the same controls around a small area so " + "wear is not piled on the same pixels: the motion is nearly "
            + "invisible and the video never moves. Drifting works without " + "dimming, and the controls still work exactly as they did.");
    public static final String MINE = pick("Мои значки", "Мої значки", "My badges");
    public static final String MINE_NOTE = pick("Порядок и что показывать. Сохраняется на сервере, видно всем.", "Порядок і що показувати. Зберігається на сервері, видно всім.",
            "The order, and which to show. Kept on the server, seen by everyone.");
    public static final String MINE_NONE = pick("У этого аккаунта пока нет значков", "У цього акаунта поки немає значків",
            "This account has no badges yet");
    public static final String MINE_WAIT = pick("Спрашиваю сервер…", "Питаю сервер…", "Asking the server...");
    public static final String MINE_SAVE = pick("Сохранить значки", "Зберегти значки", "Save my badges");
    public static final String MINE_SAVED = pick("Сохранено", "Збережено", "Saved");
    public static final String MINE_TOO_OFTEN = pick("Не чаще раза в минуту", "Не частіше разу на хвилину", "Once a minute at most");
    public static final String FREE_BADGE = pick("Бесплатный значок олда!", "Безкоштовний значок олда!", "A free badge for the old lot");
    public static final String FREE_BADGE_TEXT = pick("Бирюзовый значок «Использовал soufiT до 24-го сентября». " + "После 24-го его больше не выдадут, ни за что.",
            "Бірюзовий значок «Користувався soufiT до 24-го вересня». " + "Після 24-го його більше не видадуть, ні за що.",
            "A turquoise badge that says you were here before the 24th of " + "September. After that nobody gets one, at any price.");
    public static final String FREE_BADGE_TAKE = pick("Забрать", "Забрати", "Take it");
    public static final String FREE_BADGE_GOT = pick("Значок твой", "Значок твій", "It is yours");
    public static final String ALWAYS_DATE = pick("Дата под каждым видео", "Дата під кожним відео", "The date on every video");
    public static final String ALWAYS_DATE_NOTE = pick("Тикток показывает её только если открыть видео с профиля автора.",
            "Тікток показує її лише якщо відкрити відео з профілю автора.", "TikTok shows it only when the video was opened from a profile.");
    public static final String STORE = pick("Магазин плагинов", "Магазин плагінів", "The plugin store");
    public static final String STORE_OPEN = pick("Открыть магазин", "Відкрити магазин", "Open the store");
    public static final String STORE_WAIT = pick("Смотрю, что есть…", "Дивлюсь, що є…", "Looking...");
    public static final String STORE_GET = pick("Поставить", "Поставити", "Get it");
    public static final String STORE_ANYWAY = pick("Всё равно", "Все одно", "Anyway");
    public static final String STORE_GETTING = pick("Качаю плагин", "Завантажую плагін", "Fetching the plugin");
    public static final String STORE_GOT = pick("Готово, перезапусти приложение", "Готово, перезапусти застосунок", "Done -- restart the app");
    public static final String STORE_FAILED = pick("Не вышло", "Не вийшло", "That did not work");
    public static final String STORE_WRONG_VERSION = pick("Для другой версии TikTok", "Для іншої версії TikTok", "For a different TikTok");
    public static final String STORE_NOTE = pick("Плагины загружает владелец мода. Ставь только то, чему доверяешь: " + "плагин работает внутри тиктока с твоим аккаунтом.",
            "Плагіни завантажує власник мода. Став лише те, чому довіряєш: " + "плагін працює всередині тіктока з твоїм акаунтом.",
            "Plugins are put there by whoever runs the mod. Install what you trust: " + "a plugin runs inside TikTok with your account.");
    public static final String SAVE = pick("Сохранить", "Зберегти", "Save");
    public static final String DOWNLOADS = pick("Скачивание", "Завантаження", "Downloads");
    public static final String SEARCH = pick("Найти", "Знайти", "Search");
    public static final String SEARCH_CLEAR = pick("Очистить", "Очистити", "Clear");
    public static final String SEARCH_HINT = pick("Поиск по настройкам", "Пошук у налаштуваннях", "Search settings");
    public static final String SEARCH_RESULTS = pick("Результаты поиска", "Результати пошуку", "Search results");
    public static final String SEARCH_NONE = pick("Ничего не найдено", "Нічого не знайдено", "Nothing found");
    public static final String SEARCH_REGION = pick("Страна и регион", "Країна та регіон", "Country and region");
    public static final String SEARCH_ACCENT = pick("Цвет, палитра, сердечко", "Колір, палітра, сердечко", "Colour, palette, heart");
    public static final String SEARCH_THEME = pick("Светлая, тёмная и Material-тема", "Світла, темна та Material-тема", "Light, dark and Material theme");
    public static final String SEARCH_FEED = pick("Реклама, трансляции и фото-посты", "Реклама, трансляції та фото-пости",
            "Ads, live rooms and slideshows");
    public static final String SEARCH_VIDEO = pick("Звук, перемотка, дата и anti-burn", "Звук, перемотування, дата та anti-burn",
            "Sound, seek, date and anti-burn");
    public static final String SEARCH_DOWNLOADS = pick("Видео, аватар и стикеры", "Відео, аватар і стікери", "Video, avatar and stickers");
    public static final String SEARCH_COMMENTS = pick("Копирование целиком и выбор фрагмента", "Копіювання цілком і вибір фрагмента",
            "Copy all or select a part");
    public static final String SEARCH_BACKUP = pick("Импорт, экспорт и сброс", "Імпорт, експорт і скидання", "Import, export and reset");
    public static final String SEARCH_TEXTURES = pick("Установить и экспортировать текстурпак", "Встановити й експортувати текстурпак",
            "Install and export texture pack");
    public static final String SEARCH_FONT = pick("Шрифт и эмодзи", "Шрифт та емодзі", "Font and emoji");
    public static final String BACKUP = pick("Резервная копия", "Резервна копія", "Backup");
    public static final String BACKUP_EXPORT = pick("Экспортировать настройки", "Експортувати налаштування", "Export settings");
    public static final String BACKUP_EXPORT_NOTE = pick("JSON без аккаунта, токенов и кэша", "JSON без акаунта, токенів і кешу",
            "JSON without account, tokens or cache");
    public static final String BACKUP_IMPORT = pick("Импортировать настройки", "Імпортувати налаштування", "Import settings");
    public static final String BACKUP_IMPORT_NOTE = pick("Сначала покажем, сколько параметров будет изменено", "Спочатку покажемо, скільки параметрів буде змінено",
            "Shows how many settings will change first");
    public static final String BACKUP_APPLY = pick("Применить", "Застосувати", "Apply");
    public static final String BACKUP_READING = pick("Читаю резервную копию…", "Читаю резервну копію…", "Reading backup…");
    public static final String BACKUP_WRITING = pick("Сохраняю резервную копию…", "Зберігаю резервну копію…", "Writing backup…");
    public static final String BACKUP_DONE = pick("Готово", "Готово", "Done");
    public static final String BACKUP_FAILED = pick("Не удалось обработать резервную копию", "Не вдалося обробити резервну копію",
            "Could not process backup");
    public static final String BACKUP_RESET = pick("Сбросить категорию", "Скинути категорію", "Reset a category");
    public static final String BACKUP_RESET_NOTE = pick("Внешний вид, плеер, лента или все настройки мода", "Вигляд, плеєр, стрічка або всі налаштування мода",
            "Appearance, player, feed or every mod setting");
    public static final String BACKUP_RESET_ASK = pick("Аккаунт, токены, история и кэш не затрагиваются.", "Акаунт, токени, історія та кеш не зачіпаються.",
            "Account, tokens, history and cache are untouched.");
    public static final String RESET_APPEARANCE = pick("Внешний вид", "Вигляд", "Appearance");
    public static final String RESET_PLAYER = pick("Плеер и скачивание", "Плеєр і завантаження", "Player and downloads");
    public static final String RESET_FEED = pick("Лента и регион", "Стрічка та регіон", "Feed and region");
    public static final String RESET_ALL = pick("Все настройки мода", "Усі налаштування мода", "Every mod setting");
    public static String backupPreview(int count) {
        return pick("Будут изменены " + count + " настроек. Аккаунт, токены, история и кэш не затрагиваются.", "Буде змінено "
                + count + " налаштувань. Акаунт, токени, історія та кеш не зачіпаються.", count + " settings will change. Account, tokens, history and cache are untouched.");
    }

    public static final String NO_WATERMARK = pick("Без водяного знака", "Без водяного знака", "Without the watermark");
    public static final String DOWNLOAD_ALWAYS = pick("Сохранять можно всё", "Зберігати можна все", "Save anything");
    public static final String FEED = pick("Лента", "Стрічка", "Feed");
    public static final String HIDE_ADS = pick("Убирать рекламу", "Прибирати рекламу", "Drop the advertisements");
    public static final String PLUGINS = pick("Плагины", "Плагіни", "Plugins");
    public static final String PLUGIN_INSTALL = pick("Установить плагин", "Встановити плагін", "Install a plugin");
    public static final String PLUGIN_INSTALL_NOTE = pick("Файл .mtp", "Файл .mtp", "An .mtp file");
    public static final String PLUGIN_NONE = pick("Пока ничего не установлено.", "Поки нічого не встановлено.", "Nothing installed yet.");
    public static final String PLUGIN_WARNING = pick("Песочницы нет. Ставьте только то, чему доверяете.", "Пісочниці немає. Встановлюйте лише те, чому довіряєте.",
            "No sandbox. Install only what you trust.");
    public static final String PLUGIN_DOCS = pick("Как писать плагины", "Як писати плагіни", "Writing plugins");
    public static final String PLUGIN_DOCS_NOTE = pick("Документация на git оригинального проекта", "Документація на git оригінального проєкту",
            "The documentation on original project git");
    public static final String PLUGIN_PUBLISH_PANEL = pick("В магазин плагины выкладывает автор мода. Пришлите файл в канал.",
            "У магазин плагіни викладає автор мода. Надішліть файл у канал.", "The store is filled by the mod's author. Send the file to the channel.");
    public static final String PLUGIN_RISK_TITLE = pick("Опасный плагин", "Небезпечний плагін", "A risky plugin");
    public static final String PLUGIN_RISK_ASK = pick("В коде плагина найдено то, что обычно скрывают: запутанные имена, спрятанные вызовы или загрузка кода. Установить всё равно?",
            "У коді плагіна знайдено те, що зазвичай приховують: заплутані імена, сховані виклики або завантаження коду. Встановити все одно?",
            "The plugin's code has things that are usually hidden: scrambled names, concealed calls or loaded code. Install it anyway?");
    public static final String PLUGIN_RISK_INSTALL = pick("Установить всё равно", "Встановити все одно", "Install anyway");
    public static final String PLUGIN_AUDIT_HEAD = pick("Что делает код плагина:", "Що робить код плагіна:", "What the plugin's code does:");
    public static final String PLUGIN_AUDIT_CLEAN = pick("Ничего особенного не найдено.", "Нічого особливого не знайдено.",
            "Nothing out of the ordinary.");
    public static final String PLUGIN_AUDIT_NOTE = pick("Плагин работает с правами TikTok. Ставьте только то, чему доверяете.",
            "Плагін працює з правами TikTok. Ставте лише те, чому довіряєте.", "A plugin runs with TikTok's own rights. Install only what you trust.");
    public static final String PLUGIN_REVIEW = pick("Плагин", "Плагін", "Plugin");
    public static final String PLUGIN_INSTALLED = pick("Плагин установлен", "Плагін встановлено", "Plugin installed");
    public static final String PLUGIN_REMOVE = pick("Удалить", "Видалити", "Remove");
    public static final String PLUGIN_REMOVE_ASK = pick("Удалить плагин?", "Видалити плагін?", "Remove the plugin?");
    public static final String CANCEL = pick("Отмена", "Скасувати", "Cancel");
    public static final String PLUGIN_HOLD = pick("Долгое нажатие — удалить", "Довге натискання — видалити", "Hold to remove");
    public static final String LINKS = pick("Ссылки", "Посилання", "Links");
    public static final String CHANNEL = pick("Канал", "Канал", "Channel");
    public static final String FORUM = pick("Форум оригинального мода", "Форум оригінального моду", "Original mod's forum");
    public static final String STREAKS = pick("Серии", "Серії", "Streaks");
    public static final String STREAK_AUTO = pick("Продлевать серии сами", "Продовжувати серії самі", "Keep streaks alive");
    public static final String STREAK_PINNED_ONLY = pick("Только закреплённым друзьям", "Тільки закріпленим друзям", "Only for pinned friends");
    public static final String STREAK_HEALTH = pick("Готовность автопродления", "Готовність автопродовження", "Autoprodlenie readiness");
    public static final String STREAK_HEALTH_FIX = pick("Нажмите, чтобы разрешить точный будильник и снять экономию батареи",
            "Натисніть, щоб дозволити точний будильник і зняти економію батареї", "Tap to allow exact alarms and lift the battery saver");
    public static final String BETA = pick("бета", "бета", "beta");
    public static final String STREAK_NOTE = pick("Отправляет выбранный стикер тем, с кем серия вот-вот погаснет. " + "Не чаще раза в сутки на человека.",
            "Надсилає вибраний стікер тим, з ким серія ось-ось згасне. " + "Не частіше разу на добу на людину.", "Sends the sticker you picked to whoever the streak is about to lapse "
            + "with, at most once a day each.");
    public static final String STREAK_STICKER = pick("Чем продлевать", "Чим продовжувати", "What to send");
    public static final String STREAK_NOTHING = pick("Избранные стикеры загружаются из TikTok", "Обрані стікери завантажуються з TikTok",
            "Favourite stickers load from TikTok");
    public static final String VERSIONS = pick("Версии", "Версії", "Versions");
    public static final String SOURCE = pick("Исходники MargyT", "Вихідники MargyT", "The MargyT source");
    public static final String SOUFIT_SOURCE = pick("Исходники soufiT", "Вихідники soufiT", "The soufiT source");
    public static final String ABOUT_INTRO = pick("soufiT создал @soufied. Это форк MargyT от @narezany.\n\n" + "Форк существует по одной причине: убрать всё лишнее, по мнению soufi, "
            + "и сделать мод таким, какой нравится soufi. Меньше шума, больше того, " + "что важно, и ничего, о чём вас не просили.",
            "soufiT створив @soufied. Це форк MargyT від @narezany.\n\n" + "Цей форк існує з однієї причини: прибрати все зайве, на думку soufi, "
            + "і зробити мод таким, яким його любить soufi. Менше шуму, більше важливого " + "і нічого, про що вас не просили.",
            "soufiT is made by @soufied. It is a fork of MargyT by @narezany.\n\n" + "This fork exists for one reason: to remove everything that is useless "
            + "in soufi's opinion, and to make the mod the way soufi likes it. " + "Less noise, more of what matters, and nothing you did not ask for.");
    public static final String CHANNEL_TITLE = pick("Канал", "Канал", "Channel");
    public static final String CHANNEL_NEWS_A = pick("Новости/", "Новини/", "News/");
    public static final String CHANNEL_NEWS_B = pick("обновления", "оновлення", "updates");
    public static final String CHANNEL_SUBSCRIBE = pick("Подписаться", "Підписатися", "Subscribe");
    public static final String THANKS = pick("Благодарности", "Подяки", "Thanks");
    public static final String THANKS_NOTE = pick("Люди, без которых оригинальный мод бы не было.", "Люди, без яких оригінальний мод б не було.",
            "The people the original mod would not exist without.");
    public static final String THANKS_OWNER = pick("Владелец оригинального мода", "Власник оригінального моду", "The original mod's owner");
    public static final String THANKS_AI = pick("Помог с разработкой", "Допоміг із розробкою", "Helped with development");
    public static final String BADGES = pick("Значки", "Значки", "Badges");
    public static final String SEARCH_BADGES = pick("значок значки порядок подтверждение аккаунт", "значок значки порядок підтвердження акаунт",
            "badge badges order proof account");
    public static final String PROVE_AUTO_GOING = pick("Ставлю код в описание и жду сервер. Около минуты.", "Ставлю код в опис і чекаю сервер. Близько хвилини.",
            "Putting the code in your bio and waiting for the server. About a minute.");
    public static final String PROVE_AUTO_NO_ROOM = pick("В описании не хватает места для кода. Подтверди вручную.", "В описі бракує місця для коду. Підтвердь вручну.",
            "There is no room in the bio for the code. Do it by hand.");
    public static final String PROVE_AUTO_BUSY = pick("Проверка уже идёт", "Перевірка вже триває", "Already checking");
    public static final String PROVE_AUTO_STOPPED = pick("Само не получилось. Прежнее описание на месте.", "Само не вийшло. Попередній опис на місці.",
            "It could not be done for you. Your bio is as it was.");
    public static final String PROVE_AUTO_NO_CODE = pick("Сервер не выдал код", "Сервер не видав код", "The server gave no code");
    public static final String PROVE_AUTO_NOT_SAVED = pick("TikTok не сохранил описание", "TikTok не зберіг опис", "TikTok did not save the bio");
    public static final String PROVE_AUTO_CHANGED = pick("Описание поменялось по ходу, я его не трогаю", "Опис змінився в процесі, я його не чіпаю",
            "The bio changed meanwhile, so it is left alone");
    public static final String PROVE_AUTO_NOT_FOUND = pick("Сервер не увидел код в профиле. Описание вернул.", "Сервер не побачив код у профілі. Опис повернув.",
            "The server did not see the code on the profile. The bio is back.");
    public static final String THANKS_TTCUZ = pick("Автор ttcuz, часть функций пришла оттуда", "Автор ttcuz, частина функцій прийшла звідти",
            "Wrote ttcuz, where a share of the features came from");
    public static final String DONATE = pick("Поддержать оригинальный проект", "Підтримати оригінальний проєкт", "Support the original project");
    public static final String DONATE_NOTE = pick("Каждый ваш рубль помогает держать проект на плаву и развивать открытое "
            + "сообщество моддинга. Спасибо.", "Кожен ваш рубль допомагає тримати проєкт на плаву та розвивати відкриту "
            + "спільноту модингу. Дякуємо.", "Every rouble keeps the project afloat and goes back into the open source " + "modding community. Thank you.");
    public static final String CARD = pick("Карта", "Картка", "Card");
    public static final String TAP_TO_COPY = pick("Нажми, чтобы скопировать", "Натисни, щоб скопіювати", "Tap to copy");
    public static final String YOOMONEY = pick("ЮMoney", "ЮMoney", "YooMoney");
    public static final String YOOMONEY_NOTE = pick("Перевод напрямую", "Переказ напряму", "Straight to the transfer page");
    public static final String NO_BROWSER = pick("Нечем открыть ссылку", "Нема чим відкрити посилання", "Nothing here opens links");
    public static final String DIARY_TITLE = pick("Журнал мода", "Журнал мода", "The mod's diary");
    public static final String COPY = pick("Скопировать", "Скопіювати", "Copy");
    public static final String COPIED = pick("Скопировано", "Скопійовано", "Copied");
    public static final String CLEAR = pick("Очистить", "Очистити", "Clear");
    public static final String DIARY = pick("Что мод видел", "Що мод бачив", "What the mod saw");
    public static final String CLEARED = pick("Журнал очищен", "Журнал очищено", "The diary is empty");
    public static final String ACCOUNT = pick("Аккаунт", "Акаунт", "Account");
    public static final String ACCOUNT_ID = pick("ID", "ID", "ID");
    public static final String ACCOUNT_SEC_ID = pick("ID для ссылки", "ID для посилання", "The id a link is built from");
    public static final String ACCOUNT_UNKNOWN = pick("Пока неизвестен", "Поки невідомий", "Not seen yet");
    private static String language() {
        try {
            return Locale.getDefault().getLanguage();
        } catch (Throwable ignored) {
            return "en";
        }
    }

    public static final String DEV_PILL = pick("Плавающая кнопка", "Плаваюча кнопка", "Floating button");
    public static final String DEV_PILL_NOTE = pick("Кнопка всегда видна поверх экрана. Нажмите её, чтобы скопировать в буфер обмена то, что видно сейчас, и полный отчёт.",
            "Кнопка завжди видна поверх екрана. Натисніть її, щоб скопіювати в буфер обміну те, що видно зараз, і повний звіт.",
            "A button that stays on top of the screen. Tap it to copy what is on screen now and a full report to the clipboard.");
    public static final String DEV_PILL_LABEL = pick("Дамп", "Дамп", "Dump");
    public static final String DEV_PILL_COPIED = pick("Скопировано: экран и отчёт", "Скопійовано: екран і звіт", "Copied: screen and report");
    public static final String DEV_PILL_FAILED = pick("Дамп не скопирован", "Дамп не скопійовано", "The dump could not be copied");
    public static final String DEV_PILL_NO_CLIPBOARD = pick("Буфер обмена недоступен", "Буфер обміну недоступний", "The clipboard is not available");
    public static final String PLUGINS_SUMMARY = pick("Установлено: %d · включено: %d", "Встановлено: %d · увімкнено: %d", "Installed: %d · on: %d");
    public static final String PLUGINS_EMPTY = pick("Плагины добавляют функции, которые написали другие. Установите файл .mtp или создайте свой скрипт.",
            "Плагіни додають функції, які написали інші. Встановіть файл .mtp або створіть свій скрипт.",
            "Plugins add features written by others. Install an .mtp file or make your own script.");
    public static final String PLUGIN_ON = pick("Включён", "Увімкнено", "On");
    public static final String PLUGIN_OFF = pick("Выключен", "Вимкнено", "Off");
    public static final String PLUGIN_PROBLEM = pick("Ошибка при запуске", "Помилка під час запуску", "Failed to start");
    public static final String PLUGIN_MANAGE = pick("Действия с плагином", "Дії з плагіном", "Plugin actions");
    public static final String ABOUT_TIKTOK_BUILD = pick("TikTok", "TikTok", "TikTok");
    public static final String DEV_ON = pick("Режим разработчика включён", "Режим розробника увімкнено", "Developer mode is on");
    public static final String DEV_ALREADY = pick("Режим разработчика уже включён", "Режим розробника вже увімкнено", "Developer mode is already on");
    public static final String DEV_SWITCH = pick("Режим разработчика", "Режим розробника", "Developer mode");
    public static final String DEV_DUMP_ALL = pick("Снять дамп всех меню", "Зняти дамп усіх меню", "Dump every menu");
    public static final String DEV_DUMP_ALL_NOTE = pick("Открывает все разделы soufiT по очереди и копирует их разметку. Только переключает разделы: ничего не нажимает и не трогает экраны TikTok.",
            "Відкриває всі розділи soufiT по черзі й копіює їхню розмітку. Лише перемикає розділи: нічого не натискає і не чіпає екрани TikTok.",
            "Opens every soufiT section in turn and copies its layout. It only switches sections: it presses nothing and never touches TikTok's screens.");
    public static final String DEV_FULL_REPORT = pick("Скопировать полный отчёт", "Скопіювати повний звіт", "Copy the full report");
    public static final String DEV_FULL_NOTE = pick("Устройство, сборка, хуки, плагины и весь журнал. Без id аккаунта и токенов.",
            "Пристрій, збірка, хуки, плагіни та весь журнал. Без id акаунта й токенів.", "Device, build, hooks, plugins and the whole diary. No account id or tokens.");
    public static final String PROFILE_LOOK = pick("Вид ника", "Вигляд імені", "Nickname look");
    public static final String PROFILE_SYNC = pick("Синхронизация", "Синхронізація", "Sync");
    public static final String PROFILE_FINE = pick("Тонкая настройка", "Тонке налаштування", "Fine tuning");
    public static final String SHEET_DL_HINT = pick("Чем выше качество, тем больше файл", "Чим вища якість, тим більший файл",
            "Higher quality means a bigger file");
    public static final String ACT_DOWNLOAD_HINT = pick("Видео, фото или только звук", "Відео, фото або лише звук", "Video, photo or audio only");
    public static final String ACT_PLAYBACK_HINT = pick("Скорость и перемотка", "Швидкість і перемотування", "Speed and seeking");
    public static final String ACT_FRAME_HINT = pick("Текущий момент как фото", "Поточний момент як фото", "The current moment as a photo");
    public static final String ACT_HIDE = pick("Скрыть из ленты", "Приховати зі стрічки", "Hide from feed");
    public static final String ACT_HIDE_HINT = pick("Автор или подборка", "Автор або добірка", "Author or suggestion");
    public static final String ACT_PLAYBACK = pick("Воспроизведение", "Відтворення", "Playback");
    public static final String ACT_DOWNLOAD = pick("Скачать", "Завантажити", "Download");
    public static final String SHEET_PICK_QUALITY = pick("Выберите качество", "Оберіть якість", "Choose quality");
    public static final String SHEET_BEST = pick("Лучшее качество", "Найкраща якість", "Best quality");
    public static final String SHEET_WORST = pick("Худшее качество", "Найгірша якість", "Lowest quality");
    public static final String SHEET_PHOTOS = pick("Фото", "Фото", "Photos");
    public static final String SHEET_POST_TITLE = pick("Скачать публикацию", "Завантажити публікацію", "Download post");
    public static String allPhotos(int count) {
        return pick("Скачать все фото (" + count + ")", "Завантажити всі фото (" + count + ")", "Download all photos (" + count
                + ")");
    }

    public static String photosSaved(int saved, int total) {
        return pick("Сохранено " + saved + " из " + total + " фото", "Збережено " + saved + " із " + total + " фото", "Saved "
                + saved + " of " + total + " photos");
    }

    public static final String SHEET_AUDIO = pick("Скачать аудио", "Завантажити аудіо", "Download audio");
    public static final String SHEET_ONE_SIZE = pick("Для этого видео TikTok отдаёт только одно качество", "Для цього відео TikTok віддає лише одну якість",
            "TikTok offers only one quality for this video");
    public static final String TUTORIAL_NEXT = pick("Далее", "Далі", "Next");
    public static final String TUTORIAL_BACK = pick("Назад", "Назад", "Back");
    public static final String TUTORIAL_DONE = pick("Готово", "Готово", "Done");
    public static final String TUTORIAL_SKIP = pick("Пропустить", "Пропустити", "Skip");
    public static final String[] TUTORIAL_TITLES = {pick("Добро пожаловать в soufiT", "Ласкаво просимо в soufiT", "Welcome to soufiT"),
            pick("Поиск", "Пошук", "Search"), pick("Своя палитра", "Своя палітра", "Your own palette"), pick("Защита экрана",
            "Захист екрана", "Screen protection"), pick("Значки и градиент", "Значки й градієнт", "Badges and a gradient"),
            pick("Режим разработчика", "Режим розробника", "Developer mode")};
    public static final String[] TUTORIAL_WORDS = {pick("Все функции мода собраны в одном месте: профиль → меню → настройки TikTok → «Mod settings».",
            "Усі функції мода зібрано в одному місці: профіль → меню → налаштування TikTok → «Mod settings».", "Every feature of the mod lives in one place: profile → menu → TikTok settings → “Mod settings”."),
            pick("Начните писать в поиске в «Mod settings» — результаты появляются сразу. Страну тоже можно найти: «Регион» → «Страна», потом название или код.",
            "Почніть писати в пошуку в «Mod settings» — результати з’являються одразу. Країну теж можна знайти: «Регіон» → «Країна», далі назву або код.",
            "Start typing in the search in “Mod settings” and the results appear at once. Countries can be found too: Region → Country, then a name or a code."),
            pick("Откройте «Внешний вид» → «Акцент». Круг — доступный цвет, квадрат — выбранный.", "Відкрийте «Зовнішній вигляд» → «Акцент». Коло — доступний колір, квадрат — вибраний.",
            "Open Appearance → Accent. A circle is an available colour, a square is the chosen one."), pick("Кнопка с глазом в ленте делает кнопки прозрачными. Закрытый глаз — защита включена. Видео и фото не меняются, а сердце двойного тапа остаётся ярким.",
            "Кнопка з оком у стрічці робить кнопки прозорими. Заплющене око — захист увімкнено. Відео та фото не змінюються, а серце подвійного дотику лишається яскравим.",
            "The eye button in the feed makes the buttons transparent. A closed eye means protection is on. Videos and photos stay as they are, and the double-tap heart stays bright."),
            pick("В разделе «Профиль» витрина, значки и градиент на ник. Витрина только для показа: выберите там значок и градиент, у других ничего не меняется. Чтобы менять значки и градиент, аккаунт подтверждается один раз: код на минуту ставится в описание профиля.",
            "У розділі «Профіль» вітрина, значки й градієнт на нік. Вітрина лише для показу: оберіть там значок і градієнт, в інших нічого не змінюється. Щоб змінювати значки та градієнт, акаунт підтверджується один раз: код на хвилину ставиться в опис профілю.",
            "Under Profile you find the showcase, the badges and the gradient on your name. The showcase is only for looking: pick a badge and a gradient there and nothing changes for anyone else. Changing badges or the gradient takes proving the account once: a code sits in your bio for a minute."),
            pick("Журнал, диагностика и копии для разработки появляются, когда включён режим разработчика: пять нажатий на «soufiT» в настройках. Раздел называется «Меню разработчика».",
            "Журнал, діагностика та копії для розробки з’являються, коли увімкнено режим розробника: п’ять натискань на «soufiT» у налаштуваннях. Розділ називається «Меню розробника».",
            "The diary, diagnostics and copies for development appear when developer mode is on: five taps on “soufiT” in settings. The section is called Developer menu.")};
    public static int language3() {
        return RU ? 0 : UK ? 1 : 2;
    }

    private static String pick(String russian, String ukrainian, String english) {
        if (RU) return russian;
        if (UK) return ukrainian;
        return english;
    }
}
