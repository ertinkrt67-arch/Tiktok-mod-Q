package bk.soufit.core;
import java.util.HashMap;
import java.util.Map;
public final class Local {
    private Local() {} private static final Map<String, String[]> BOOK = new HashMap<String, String[]>();
    public static String of(String russian) {
        int language = Text.language3();
        if (language == 0 || russian == null) return russian;
        String[] said = BOOK.get(russian);
        return said == null ? russian : said[language - 1];
    }

    private static void add(String russian, String ukrainian, String english) {
        BOOK.put(russian, new String[] {ukrainian, english});
    }

    static {
        add("Сервер бейджей", "Сервер бейджів", "Badge server");
        add("Адрес сервера", "Адреса сервера", "Server address");
        add("Загрузить сейчас", "Завантажити зараз", "Load now");
        add("Сама ничего не загружается", "Нічого не завантажується само", "Nothing loads by itself");
        add("Журнал сбоев на телефоне", "Журнал збоїв на телефоні", "Crash log on this phone");
        add("Пишется только на этом телефоне. Никуда не отправляется.", "Пишеться лише на цей телефон. Нікуди не надсилається.",
                "Written only to this phone. Never sent anywhere.");
        add("Скопировать журнал сбоев", "Копіювати журнал збоїв", "Copy crash log");
        add("Журнал сбоев", "Журнал збоїв", "Crash log");
        add("Очистить журнал сбоев", "Очистити журнал збоїв", "Clear crash log");
        add("Форк основан на MargyT", "Форк створено на основі MargyT", "This fork is based on MargyT");
        add("не задан", "не задано", "not set");
        add("Сначала задайте адрес сервера", "Спочатку вкажіть адресу сервера", "Set the server address first");
        add("Запрос отправлен", "Запит надіслано", "Request sent");
        add("Восстановить био после прерванной проверки", "Відновити біо після перерваної перевірки", "Restore bio after an interrupted check");
        add("Ничего не делает, если проверка не прерывалась", "Нічого не робить, якщо перевірку не перервано", "Does nothing unless a check was interrupted");
        add("Восстановление запрошено", "Відновлення запитано", "Restore requested");
        add("Пусто", "Порожньо", "Empty");
        add("Адрес должен начинаться с https://", "Адреса має починатися з https://", "The address must start with https://");
        add("Код плагина не удалось разобрать: ", "Код плагіна не вдалося розібрати: ", "The plugin's code could not be read: ");
        add("Задачи  ·  Свернуть", "Завдання  ·  Згорнути", "Tasks  ·  Collapse");
        add("Нет сетевых задач", "Немає мережевих завдань", "No network tasks");
        add("Загрузка", "Завантаження", "Downloading");
        add("Отменить", "Скасувати", "Cancel");
        add("Повторить", "Повторити", "Retry");
        add("Загрузка завершена", "Завантаження завершено", "Download finished");
        add("Лучшее", "Найкраще", "Best");
        add(" кбит/с", " кбіт/с", " kbit/s");
        add("Фото ", "Фото ", "Photo ");
        add("Отмена…", "Скасування…", "Cancelling…");
        add("Готово", "Готово", "Done");
        add("Без аккаунта", "Без акаунта", "No account");
        add("Аккаунт ", "Акаунт ", "Account ");
        add("Мод", "Мод", "Mod");
        add("Этот аккаунт", "Цей акаунт", "This account");
        add("Завершено", "Завершено", "Finished");
        add("Отменено", "Скасовано", "Cancelled");
        add("Ошибка", "Помилка", "Failed");
        add("Сохранять комментарии", "Зберігати коментарі", "Save comments");
        add("Сохранённые комментарии", "Збережені коментарі", "Saved comments");
        add("Своё значение", "Власне значення", "Custom value");
        add(" видео", " відео", " videos");
        add("Количество видео", "Кількість відео", "Number of videos");
        add("Отмена", "Скасувати", "Cancel");
        add("Выбрать", "Вибрати", "Choose");
        add("Введите число от 1 до 10000", "Введіть число від 1 до 10000", "Enter a number from 1 to 10000");
        add("Не удалось выбрать количество", "Не вдалося вибрати кількість", "Could not set the number");
        add("500 видео", "500 відео", "500 videos");
        add("1000 видео", "1000 відео", "1000 videos");
        add("Количество видео: 1–10000", "Кількість відео: 1–10000", "Number of videos: 1–10000");
        add("Не удалось сохранить комментарии. Повторите загрузку при подключении к интернету.", "Не вдалося зберегти коментарі. Повторіть завантаження, коли буде інтернет.",
                "Could not save the comments. Download again when you are online.");
        add("Количество комментариев: 1–1000", "Кількість коментарів: 1–1000", "Number of comments: 1–1000");
        add("Больше комментариев — дольше загрузка и больше места. Популярные в приоритете. Лайки сохраняются на момент загрузки. Применяется к следующим офлайн-загрузкам.",
                "Більше коментарів — довше завантаження і більше місця. Популярні мають пріоритет. Вподобання зберігаються на момент завантаження. Діє для наступних офлайн-завантажень.",
                "More comments take longer to download and more space. Popular ones come first. Likes are saved as they were when downloaded. Applies to the next offline downloads.");
        add("Комментарии офлайн", "Коментарі офлайн", "Offline comments");
        add("Сохранить", "Зберегти", "Save");
        add("Введите число от 1 до 1000", "Введіть число від 1 до 1000", "Enter a number from 1 to 1000");
        add("ОК", "ОК", "OK");
        add("Видео ", "Відео ", "Video ");
        add("Сохранённых комментариев пока нет. Включите сохранение перед загрузкой офлайн-видео.", "Збережених коментарів поки немає. Увімкніть збереження перед завантаженням офлайн-відео.",
                "No saved comments yet. Turn saving on before downloading videos for offline.");
        add("Закрыть", "Закрити", "Close");
        add("Очередь занята. Повторите чуть позже.", "Черга зайнята. Повторіть трохи пізніше.", "The queue is busy. Try again a little later.");
        add("Комментарии ещё загружаются.", "Коментарі ще завантажуються.", "The comments are still downloading.");
        add("Для этого видео комментарии не сохранены. Включите сохранение перед его офлайн-загрузкой.", "Для цього відео коментарі не збережено. Увімкніть збереження перед його офлайн-завантаженням.",
                "No comments were saved for this video. Turn saving on before downloading it for offline.");
        add("Сохранено: ", "Збережено: ", "Saved: ");
        add(" · лайки на момент загрузки", " · вподобання на момент завантаження", " · likes as of the download");
        add("У видео нет доступных комментариев.", "У відео немає доступних коментарів.", "This video has no comments available.");
        add("Поделиться ZIP", "Поділитися ZIP", "Share as ZIP");
        add("Не удалось прочитать архив комментариев.", "Не вдалося прочитати архів коментарів.", "Could not read the comment archive.");
        add("Архив комментариев", "Архів коментарів", "Comment archive");
        add("Не удалось открыть приложение для передачи ZIP.", "Не вдалося відкрити застосунок для передачі ZIP.", "Could not open an app to share the ZIP.");
        add("Превышен размер файла плагина", "Перевищено розмір файлу плагіна", "The plugin file is too large");
        add("Загрузка дополнительного исполняемого кода", "Завантаження додаткового виконуваного коду", "Loads more executable code");
        add("Рефлексия: часть действий нельзя установить статически", "Рефлексія: частину дій не можна встановити статично",
                "Reflection: some of what it does cannot be told from the code");
        add("Доступ к сети", "Доступ до мережі", "Network access");
        add("WebView и возможное выполнение JavaScript", "WebView і можливе виконання JavaScript", "A WebView, and possibly running JavaScript");
        add("Доступ к личным данным устройства", "Доступ до особистих даних пристрою", "Access to personal data on the device");
        add("Доступ к буферу обмена", "Доступ до буфера обміну", "Clipboard access");
        add("Прямой доступ к файлам приложения", "Прямий доступ до файлів застосунку", "Direct access to the app's files");
        add("Запуск системных команд", "Запуск системних команд", "Runs system commands");
        add("Загрузка нативных библиотек", "Завантаження нативних бібліотек", "Loads native libraries");
        add("Поиск классов по имени: возможна скрытая загрузка API", "Пошук класів за назвою: можливе приховане завантаження API",
                "Looks classes up by name: may reach APIs out of sight");
        add("Сеть через API мода", "Мережа через API мода", "Network through the mod's API");
        add("Доступ к контексту TikTok", "Доступ до контексту TikTok", "Access to TikTok's context");
        add("Загрузка классов по имени", "Завантаження класів за назвою", "Loads classes by name");
        add("Некорректная таблица DEX", "Некоректна таблиця DEX", "Malformed DEX table");
        add("Уровень риска: ", "Рівень ризику: ", "Risk level: ");
        add("высокий", "високий", "high");
        add("средний", "середній", "medium");
        add("низкий", "низький", "low");
        add("Имена классов запутаны (обфускация): ", "Імена класів заплутані (обфускація): ", "Class names are scrambled (obfuscation): ");
        add("Точка входа плагина не найдена в коде: ", "Точку входу плагіна не знайдено в коді: ", "The plugin's entry class is missing from its code: ");
        add("Скрытый текст: закодированные строки, которые читаются как текст или код: ", "Прихований текст: закодовані рядки, які читаються як текст або код: ",
                "Hidden text: encoded strings that read as text or code: ");
        add(" шт.", " шт.", " found");
        add("Адреса в коде: ", "Адреси в коді: ", "Addresses in the code: ");
        add("Имя системного класса записано строкой: вызов скрыт от проверки", "Ім'я системного класу записано рядком: виклик приховано від перевірки",
                "A system class name is written as text: a call is hidden from checks");
        add("Поиск root-доступа", "Пошук root-доступу", "Looks for root access");
        add("Закодированные строки: ", "Закодовані рядки: ", "Encoded strings: ");
        add("Нативные методы вне проверки Java-кода", "Нативні методи поза перевіркою Java-коду", "Native methods, which this check cannot read");
        add("Некорректный DEX", "Некоректний DEX", "Malformed DEX");
        add("Неподдерживаемая версия DEX", "Непідтримувана версія DEX", "Unsupported DEX version");
        add("Некорректный заголовок DEX", "Некоректний заголовок DEX", "Malformed DEX header");
        add("Повреждён DEX: SHA-1", "Пошкоджено DEX: SHA-1", "Corrupt DEX: SHA-1");
        add("Повреждён DEX: checksum", "Пошкоджено DEX: checksum", "Corrupt DEX: checksum");
        add("Слишком большое значение DEX", "Завелике значення DEX", "DEX value too large");
        add("Таблица за границами DEX", "Таблиця за межами DEX", "Table outside the DEX");
        add("Слишком сложный DEX для проверки", "Занадто складний DEX для перевірки", "DEX too complex to check");
        add("Переполнение DEX", "Переповнення DEX", "DEX overflow");
        add("Некорректный ULEB128", "Некоректний ULEB128", "Malformed ULEB128");
        add("Индекс строки DEX", "Індекс рядка DEX", "DEX string index");
        add("Слишком большие строки DEX", "Завеликі рядки DEX", "DEX strings too large");
        add("Индекс типа DEX", "Індекс типу DEX", "DEX type index");
        add("Нет ссылки на файл плагина", "Немає посилання на файл плагіна", "No link to the plugin file");
        add("Слишком длинный адрес каталога", "Задовга адреса каталогу", "The catalog address is too long");
        add("Каталог и файлы должны использовать HTTPS без логина в адресе", "Каталог і файли мають використовувати HTTPS без логіна в адресі",
                "The catalog and its files must use HTTPS with no login in the address");
        add("Нужен аккаунт создателя", "Потрібен акаунт автора", "The author's account is needed");
        add("Плагин не установлен", "Плагін не встановлено", "The plugin is not installed");
        add("Пока пусто — нажмите +", "Поки порожньо — натисніть +", "Nothing yet — tap +");
        add("Добавить в магазин", "Додати в магазин", "Add to the store");
        add("Данные плагина", "Дані плагіна", "Plugin details");
        add("Новый плагин", "Новий плагін", "New plugin");
        add("Код сохраняется. После изменения плагин нужно включить заново.", "Код зберігається. Після зміни плагін треба увімкнути знову.",
                "The code is kept. After a change the plugin has to be switched on again.");
        add("Название", "Назва", "Name");
        add("Версия", "Версія", "Version");
        add("Автор", "Автор", "Author");
        add("ID: латиница, например my.plugin", "ID: латиниця, наприклад my.plugin", "ID: Latin letters, like my.plugin");
        add("Описание, до 240 символов", "Опис, до 240 символів", "Description, up to 240 characters");
        add("Версия TikTok (необязательно)", "Версія TikTok (необов'язково)", "TikTok version (optional)");
        add("Кнопки", "Кнопки", "Buttons");
        add("Сообщение, копирование текста или окно. До 16.", "Повідомлення, копіювання тексту або вікно. До 16.", "A message, copying text, or a window. Up to 16.");
        add("Не больше 16 кнопок", "Не більше 16 кнопок", "No more than 16 buttons");
        add("Имена", "Імена", "Names");
        add("Другое имя у аккаунта. UID — число из ссылки на профиль.", "Інше ім'я в акаунта. UID — число з посилання на профіль.",
                "Another name for an account. The UID is the number in the profile link.");
        add("Не больше 16 имён", "Не більше 16 імен", "No more than 16 names");
        add("Цвета", "Кольори", "Colours");
        add("Один цвет заменяется другим по всему приложению.", "Один колір замінюється іншим у всьому застосунку.", "One colour is replaced by another across the app.");
        add("Не больше 16 цветов", "Не більше 16 кольорів", "No more than 16 colours");
        add("Показать JSON", "Показати JSON", "Show JSON");
        add("Исходник", "Вихідний код", "Source");
        add("Применить", "Застосувати", "Apply");
        add("Публикация в магазине доступна только аккаунту создателя.", "Публікація в магазині доступна лише акаунту автора.",
                "Only the author's account can publish to the store.");
        add("Опубликовать в магазине", "Опублікувати в магазині", "Publish to the store");
        add("В магазин", "У магазин", "To the store");
        add("Иконка плагина", "Іконка плагіна", "Plugin icon");
        add("Иконка", "Іконка", "Icon");
        add("Выбрать другую", "Вибрати іншу", "Choose another");
        add("Убрать", "Прибрати", "Remove");
        add("На устройстве нет выбора файлов", "На пристрої немає вибору файлів", "This device has no file picker");
        add("Название кнопки", "Назва кнопки", "Button label");
        add("Сообщение", "Повідомлення", "Message");
        add("Копировать", "Копіювати", "Copy");
        add("Окно", "Вікно", "Window");
        add("Текст", "Текст", "Text");
        add("UID аккаунта", "UID акаунта", "Account UID");
        add("Показывать как", "Показувати як", "Show as");
        add("Заполните название, версию и автора", "Заповніть назву, версію й автора", "Fill in the name, version and author");
        add("Для магазина размер .mtp должен быть не больше 1 МБ", "Для магазину розмір .mtp має бути не більше 1 МБ", "For the store an .mtp must be 1 MB or less");
        add("Опубликовано в магазине", "Опубліковано в магазині", "Published to the store");
        add("Сохранено", "Збережено", "Saved");
        add("Не сохранено", "Не збережено", "Not saved");
        add("Проверьте поля", "Перевірте поля", "Check the fields");
        add("Не удалось прочитать иконку", "Не вдалося прочитати іконку", "Could not read the icon");
        add("Иконка слишком большая", "Іконка завелика", "The icon is too large");
        add("Файл больше ", "Файл більший за ", "The file is over ");
        add(" МБ", " МБ", " MB");
        add(" КБ", " КБ", " KB");
        add(" — магазин такой не примет", " — магазин такий не прийме", " — the store will not take it");
        add("В файле лишнее: ", "У файлі зайве: ", "The file has something extra: ");
        add(". Нужны только manifest.json, classes.dex и icon.png", ". Потрібні лише manifest.json, classes.dex та icon.png",
                ". Only manifest.json, classes.dex and icon.png belong in it");
        add("Файл повреждён", "Файл пошкоджено", "The file is damaged");
        add("Это не плагин: нет manifest.json или classes.dex", "Це не плагін: немає manifest.json або classes.dex", "Not a plugin: no manifest.json or classes.dex");
        add("В manifest.json неверный ID", "У manifest.json неправильний ID", "The ID in manifest.json is wrong");
        add("Ничего особенного не найдено.", "Нічого особливого не знайдено.", "Nothing out of the ordinary.");
        add("Не удалось подготовить плагин", "Не вдалося підготувати плагін", "Could not prepare the plugin");
        add("Плагин содержит лишние, вложенные или повторяющиеся файлы", "Плагін містить зайві, вкладені або повторювані файли",
                "The plugin has extra, nested or repeated files");
        add("Недопустимый путь плагина", "Неприпустимий шлях плагіна", "A path in the plugin is not allowed");
        add("Плагин превышает ограничение размера", "Плагін перевищує обмеження розміру", "The plugin is over the size limit");
        add("В плагине нет manifest.json или classes.dex", "У плагіні немає manifest.json або classes.dex", "The plugin has no manifest.json or classes.dex");
        add("Плагин не найден", "Плагін не знайдено", "Plugin not found");
        add("Некорректный manifest.json", "Некоректний manifest.json", "Malformed manifest.json");
        add("Плагин изменился после проверки", "Плагін змінився після перевірки", "The plugin changed after it was looked at");
        add("Не удалось сохранить предыдущий плагин", "Не вдалося зберегти попередній плагін", "Could not keep the previous plugin");
        add("Невозможно прочитать плагин", "Неможливо прочитати плагін", "Cannot read the plugin");
        add("Предпросмотр", "Попередній перегляд", "Preview");
        add("Неизвестное поле команды", "Невідоме поле команди", "Unknown field in a command");
        add("Некорректное поле: ", "Некоректне поле: ", "Malformed field: ");
        add("Управляющие символы запрещены", "Керівні символи заборонено", "Control characters are not allowed");
        add("Слишком вложенный код", "Занадто вкладений код", "The code is nested too deep");
        add("Плагин больше 64 КБ", "Плагін більший за 64 КБ", "The plugin is over 64 KB");
        add("Неизвестный формат", "Невідомий формат", "Unknown format");
        add("Иконка должна быть PNG до 32 КБ", "Іконка має бути PNG до 32 КБ", "The icon must be a PNG of 32 KB or less");
        add("Некорректный ID", "Некоректний ID", "Malformed ID");
        add("Ожидается список: ", "Очікується список: ", "A list is expected: ");
        add("Не больше 16 команд в списке", "Не більше 16 команд у списку", "No more than 16 commands in a list");
        add("Команда не разрешена: ", "Команду не дозволено: ", "Command not allowed: ");
        add("Нужен ID аккаунта", "Потрібен ID акаунта", "An account ID is needed");
        add("Цвет: #RRGGBB", "Колір: #RRGGBB", "Colour: #RRGGBB");
        add("Не больше 16 плагинов", "Не більше 16 плагінів", "No more than 16 plugins");
        add("Выключить", "Вимкнути", "Switch off");
        add("Включить", "Увімкнути", "Switch on");
        add("Редактировать", "Редагувати", "Edit");
        add("Ещё", "Ще", "More");
        add("Отправить на проверку", "Надіслати на перевірку", "Send for review");
        add("Копировать исходник", "Копіювати вихідний код", "Copy the source");
        add("Удалить", "Видалити", "Delete");
        add("Скопировано", "Скопійовано", "Copied");
        add("Задачи", "Завдання", "Tasks");
        add("Сохранено в ", "Збережено в ", "Saved to ");
        add("Подсказки", "Підказки", "Hints");
        add("Обучение", "Навчання", "Tutorial");
        add("Глаз в ленте и на видео включает и выключает затемнение на лету. Поле ввода комментария не затемняется. Нижняя панель прячется при затемнении, только если включено «Прятать нижнюю панель» в «Внешнем виде». Нажатие на её место возвращает её на несколько секунд.",
                "Око у стрічці та на відео вмикає й вимикає затемнення на льоту. Поле введення коментаря не затемнюється. Нижня панель під час затемнення ховається й повертається на кілька секунд, якщо натиснути на її місце.",
                "The eye in the feed and on a video turns the dimming on and off as you go. The comment box is never dimmed. The bottom bar goes while the screen is dimmed, and comes back for a few seconds when you touch where it was.");
        add("Добавить плагин", "Додати плагін", "Add a plugin");
        add("Пока нет установленных плагинов", "Поки немає встановлених плагінів", "No plugins installed yet");
        add("Включён", "Увімкнено", "On");
        add("Выключен", "Вимкнено", "Off");
        add("Время отправки", "Час надсилання", "Time to send");
        add("Отчёты о сбоях", "Звіти про збої", "Crash reports");
        add("Отправлять разработчику технический стек сбоя. Без сообщений, аккаунта и текста ошибки.", "Надсилати розробнику технічний стек збою. Без повідомлень, акаунта й тексту помилки.",
                "Send the developer the technical stack of a crash. No messages, no account, no error text.");
        add("Выше", "Вище", "Up");
        add("Ниже", "Нижче", "Down");
        add("Укажите цвет в формате #RRGGBB", "Вкажіть колір у форматі #RRGGBB", "Give the colour as #RRGGBB");
        add("Создать из кнопок", "Створити з кнопок", "Build from buttons");
        add("Вставить JSON", "Вставити JSON", "Paste JSON");
        add("Установить файл .mtp", "Встановити файл .mtp", "Install an .mtp file");
        add("Магазин", "Магазин", "Store");
        add("Документация", "Документація", "Documentation");
        add("Удерживайте для действий", "Утримуйте для дій", "Hold for actions");
        add("Удалить с устройства", "Видалити з пристрою", "Delete from the device");
        add("Управление магазином", "Керування магазином", "Manage the store");
        add("Включите плагин, чтобы открыть его настройки.", "Увімкніть плагін, щоб відкрити його налаштування.", "Switch the plugin on to open its settings.");
        add("Плагин не предоставил настройки. Версия ", "Плагін не надав налаштувань. Версія ", "The plugin has no settings. Version ");
        add("Плагин включён, но не запущен: ", "Плагін увімкнено, але не запущено: ", "The plugin is on but not running: ");
        add("Плагин включён, но ещё не запущен в этом сеансе.", "Плагін увімкнено, але ще не запущено в цьому сеансі.", "The plugin is on but has not started in this session yet.");
        add("Запустить заново", "Запустити знову", "Start again");
        add("Назад", "Назад", "Back");
        add("Плагин не принят", "Плагін не прийнято", "The plugin was not accepted");
        add("Настройки", "Налаштування", "Settings");
        add("Включено", "Увімкнено", "On");
        add("Выключено", "Вимкнено", "Off");
        add("Включена", "Увімкнена", "On");
        add("Выключена", "Вимкнена", "Off");
        add("Реклама скрыта", "Рекламу приховано", "Ads hidden");
        add("Реклама разрешена", "Рекламу дозволено", "Ads allowed");
        add("Без водяного знака", "Без водяного знака", "No watermark");
        add("Обычное сохранение", "Звичайне збереження", "Ordinary saving");
        add("Откройте свой профиль", "Відкрийте свій профіль", "Open your profile");
        add("Открыть настройки", "Відкрити налаштування", "Open settings");
        add("Оформление", "Оформлення", "Appearance");
        add("Профиль", "Профіль", "Profile");
        add("Лента и видео", "Стрічка та відео", "Feed and video");
        add("Инструменты", "Інструменти", "Tools");
        add("О проекте", "Про проєкт", "About");
        add("Плагины", "Плагіни", "Plugins");
        add("Друзья и чаты", "Друзі та чати", "Friends and chats");
        add("Скачивание, резервные копии, диагностика и журнал. Сохраните копию перед экспериментами.", "Завантаження, резервні копії, діагностика й журнал. Збережіть копію перед експериментами.",
                "Downloads, backups, diagnostics and the diary. Save a backup before experimenting.");
        add("Понятно", "Зрозуміло", "Got it");
        add("Скачивание, копии и диагностика", "Завантаження, копії та діагностика", "Downloads, backups and diagnostics");
        add("Защита экрана", "Захист екрана", "Screen protection");
        add("Потяните вниз, чтобы закрыть", "Потягніть униз, щоб закрити", "Pull down to close");
        add("Каталог ещё не подключён", "Каталог ще не підключено", "No catalog connected yet");
        add("Подключить каталог", "Підключити каталог", "Connect a catalog");
        add("Каталог · изменить адрес", "Каталог · змінити адресу", "Catalog · change the address");
        add("Обновление…", "Оновлення…", "Refreshing…");
        add("Обновить", "Оновити", "Refresh");
        add("В каталоге пока пусто", "У каталозі поки порожньо", "The catalog is empty for now");
        add("Скачанные плагины остаются выключенными, пока вы их не включите.", "Завантажені плагіни лишаються вимкненими, доки ви їх не ввімкнете.",
                "Downloaded plugins stay off until you switch them on.");
        add("Магазин плагинов", "Магазин плагінів", "Plugin store");
        add("SHA-256 файла не совпадает с каталогом", "SHA-256 файлу не збігається з каталогом", "The file's SHA-256 does not match the catalog");
        add("ID плагина отличается от каталога", "ID плагіна відрізняється від каталогу", "The plugin's ID is not the one in the catalog");
        add("Каталог плагинов", "Каталог плагінів", "Plugin catalog");
        add("Каталог soufiT подключён по умолчанию. Можно указать другой HTTPS JSON-каталог; пустой адрес отключает магазин.",
                "Каталог soufiT підключено за замовчуванням. Можна вказати інший HTTPS JSON-каталог; порожня адреса вимикає магазин.",
                "soufiT's catalog is connected by default. You can give another HTTPS JSON catalog; an empty address switches the store off.");
        add("Серии", "Серії", "Streaks");
        add("Серия ждёт продления", "Серія чекає на продовження", "A streak is waiting");
        add("Откройте TikTok: автопродление сработает сразу", "Відкрийте TikTok: автопродовження спрацює одразу", "Open TikTok: it will be kept alive straight away");
        add("Автопродление выключено", "Автопродовження вимкнено", "Keeping streaks alive is off");
        add("точный будильник не разрешён (Android 12+: «Будильники и напоминания»)", "точний будильник не дозволено (Android 12+: «Будильники та нагадування»)",
                "exact alarms are not allowed (Android 12+: Alarms & reminders)");
        add("экономия батареи не отключена для приложения", "економію батареї не вимкнено для застосунку", "battery saving is still on for the app");
        add("уведомления приложения выключены", "сповіщення застосунку вимкнено", "the app's notifications are off");
        add("время не выбрано", "час не вибрано", "no time chosen");
        add("Готово: отправка в ", "Готово: надсилання о ", "Ready: sending at ");
        add("Проверьте: ", "Перевірте: ", "Check: ");
        add("Состояние неизвестно", "Стан невідомий", "State unknown");
        add("Когда серия ждёт продления", "Коли серія чекає на продовження", "When a streak is waiting");
        add("В %02d:%02d", "О %02d:%02d", "At %02d:%02d");
        add("Время автопродления", "Час автопродовження", "Time to keep streaks alive");
        add("В выбранное время устройства — одна отправка в день друзьям с действующим огоньком. Для точного запуска в фоне разрешите будильники и работу без ограничений батареи.",
                "У вибраний час пристрою — одне надсилання на день друзям із чинним вогником. Для точного запуску у фоні дозвольте будильники й роботу без обмежень батареї.",
                "At the chosen time on this device, one send a day to friends with a live flame. For it to run on time in the background, allow alarms and unrestricted battery use.");
        add("Без расписания", "Без розкладу", "No schedule");
        add("Обновление soufiT", "Оновлення soufiT", "soufiT update");
        add(" с", " с", " s");
        add("Графити", "Графіті", "Graffiti");
        add("Блестящий", "Блискучий", "Shiny");
        add("Косплей на ТикТок", "Косплей на ТікТок", "TikTok cosplay");
        add("Глитч", "Глітч", "Glitch");
        add("Точечная", "Крапкова", "Dotted");
        add("Терминал", "Термінал", "Terminal");
        add("Гламурная", "Гламурна", "Glamour");
        add("Толстый тилкайо", "Товстий тілкайо", "Fat tilcayo");
    }
}
