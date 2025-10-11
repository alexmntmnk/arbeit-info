Конечно, давайте подробно разберём TestTemplate. Это мощный, но более сложный механизм JUnit 5, который даёт полный контроль над генерацией тестовых запусков.

Вы используете TestTemplate, когда стандартного @ParameterizedTest недостаточно. Например, если логика генерации тестов очень сложная, зависит от внешних условий, или если вам нужно динамически применять разные расширения (extensions) к разным запускам.

Для вашей задачи это избыточно, но это отличный способ глубже понять, как работает JUnit 5.

## Основные компоненты
Для создания тестов с помощью TestTemplate нам понадобятся три ключевых элемента:

@TestTemplate: Аннотация, которая помечает метод не как обычный тест, а как шаблон для будущих тестов.

TestTemplateInvocationContextProvider: Это "фабрика" тестовых запусков. Её задача — создать и предоставить JUnit один или несколько "контекстов вызова". Каждый контекст — это, по сути, один запуск вашего теста с уникальным набором данных и конфигурацией.

TestTemplateInvocationContext: Объект, описывающий один конкретный запуск теста. Он содержит:

Отображаемое имя теста.

Параметры, которые будут переданы в метод-шаблон.

Дополнительные расширения (если нужны).

## Пошаговая реализация
Давайте реализуем вашу задачу с нуля, используя этот подход.

### Шаг 1: Создание модели данных
Чтобы код был чистым, создадим Java record для хранения всех параметров одного тестового запуска. Это удобнее, чем передавать массив Object[].

Java

// Модель данных для одного тестового запуска
record TestCaseData(String tmsLink, String tag, String story, String jsonPath, String orderId) {}
### Шаг 2: Реализация TestTemplateInvocationContextProvider
Это главный класс, который будет генерировать наши тесты. Назовём его IpCreateTestContextProvider.

Внутри него мы:

Определяем список наших тестовых данных (TestCaseData).

Создаём поток (Stream) из этих данных.

Для каждого набора данных создаём свой TestTemplateInvocationContext.

Java

import org.junit.jupiter.api.extension.*;
import java.util.List;
import java.util.stream.Stream;

// Это наша "фабрика", которая будет поставлять запуски для @TestTemplate
public class IpCreateTestContextProvider implements TestTemplateInvocationContextProvider {

    @Override
    public boolean supportsTestTemplate(ExtensionContext context) {
        // Мы можем добавить логику, чтобы этот провайдер работал только для определенных методов.
        // Для простоты, он будет работать для всех @TestTemplate, к которым его подключат.
        return true;
    }

    @Override
    public Stream<TestTemplateInvocationContext> provideInvocationContexts(ExtensionContext context) {
        // 1. Определяем все наши тестовые кейсы
        List<TestCaseData> testCases = List.of(
            new TestCaseData("AEP-T15584", "15584", "15584 ИП CREATE", "data/ГД/15584 DocumentDescriptionDto.json", "15584-Order"),
            new TestCaseData("AEP-T15585", "15585", "15585 ИП CREATE", "data/ГД/15585 DocumentDescriptionDto.json", "15585-Order")
        );

        // 2. Для каждого кейса создаем свой контекст вызова
        return testCases.stream().map(testCase -> new IpCreateTestInvocationContext(testCase));
    }
}
### Шаг 3: Реализация TestTemplateInvocationContext
Теперь создадим класс, который описывает один конкретный запуск. Он будет отвечать за передачу параметров в тестовый метод. Обычно его делают вложенным или в том же файле, что и провайдер.

Java

// Класс, описывающий ОДИН запуск теста.
class IpCreateTestInvocationContext implements TestTemplateInvocationContext {

    private final TestCaseData testCase;

    public IpCreateTestInvocationContext(TestCaseData testCase) {
        this.testCase = testCase;
    }

    @Override
    public String getDisplayName(int invocationIndex) {
        // Формируем динамическое имя для каждого теста
        return String.format("[%s] История: %s", testCase.tag(), testCase.story());
    }

    @Override
    public List<Extension> getAdditionalExtensions() {
        // Здесь происходит магия передачи параметров в метод.
        // Мы создаем ParameterResolver, который "знает", как сопоставить
        // тип параметра в методе с нашими данными.
        return List.of(new ParameterResolver() {
            @Override
            public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
                // Поддерживаем все параметры типа String
                return parameterContext.getParameter().getType().equals(String.class);
            }

            @Override
            public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
                // Возвращаем нужный параметр по его имени
                String parameterName = parameterContext.getParameter().getName();
                return switch (parameterName) {
                    case "tmsLink" -> testCase.tmsLink();
                    case "tag" -> testCase.tag();
                    case "story" -> testCase.story();
                    case "jsonPath" -> testCase.jsonPath();
                    case "orderId" -> testCase.orderId();
                    default -> throw new IllegalArgumentException("Unknown parameter: " + parameterName);
                };
            }
        });
    }
}
Важное замечание: Чтобы resolveParameter мог определять параметры по имени (parameterName), в настройках компилятора Java должен быть включен флаг -parameters. Если его нет, имена будут вида arg0, arg1 и т.д.

## Шаг 4: Создание и запуск теста
Теперь, когда вся инфраструктура готова, сам тест становится очень простым. Мы просто подключаем наш провайдер через аннотацию @ExtendWith.

Java

import io.qameta.allure.Allure;
import io.qameta.allure.Issue;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

// Подключаем наш провайдер к тестовому классу
@ExtendWith(IpCreateTestContextProvider.class)
class CombinedIpCreateTemplateTest {

    @Issue("AEP-58597")
    @TestTemplate // Используем @TestTemplate вместо @Test или @ParameterizedTest
    void combinedIpCreateTest(String tmsLink, String tag, String story, String jsonPath, String orderId) {
        // ВАЖНО: Даже при таком подходе, для интеграции с Allure
        // мы все равно используем его программный API.
        // Это самый надежный способ.
        Allure.tms(tmsLink, tmsLink);
        Allure.story(story);
        Allure.label("tag", tag);

        Allure.step("Создание документа по пути: " + jsonPath, () -> {
            final var createDocument = createDocument(jsonPath, orderId);
            // ... ваши проверки ...
        });
    }

    private Object createDocument(String jsonPath, String orderId) {
        System.out.printf("Creating document with path: %s and orderId: %s%n", jsonPath, orderId);
        return new Object();
    }
}

## Итог и сравнение
Плюсы TestTemplate:

Максимальная гибкость: Источником данных может быть что угодно: файл, база данных, ответ от API, сложная логика.

Динамическая конфигурация: Вы можете добавлять разные расширения (Extension) для разных тестовых запусков.

Минусы:

Многословность: Требуется написать значительно больше кода (провайдер, контекст, модель данных) по сравнению с одной аннотацией @CsvSource.

Сложность: Легче допустить ошибку. Требует более глубокого понимания архитектуры JUnit 5.

Вывод: Для вашей конкретной задачи TestTemplate — это явный "overkill" (избыточное усложнение). Однако, если в будущем вам понадобится генерировать тесты из динамического источника (например, прочитать список файлов из папки и создать тест для каждого), то TestTemplate будет идеальным инструментом.
