package itis.parsing2;


/*
Вы пишите систему для интеграции с готовой структурой заказчика и обработки данных о фабриках корпораций.
!!КЛАСС FACTORY и классы ошибок менять НЕЛЬЗЯ (совсем). Считается что они являются частью готовой библиотеки!!

 * В папке resources находятся две папки, в них два файла которые содержат примеры данных, которые нужно обрабатывать.
 * Данные записаны в формате одна строка - одно поле. Название поля и значение разделяются символом ":".
 * Над некоторыми полями стоят аннотации, которые служат для проверки даннных поля. Подробное описание доступно в классе аннотации.
 * В отличии от изначального варианта, здесь на вход подается путь к ДИРЕКТОРИИ. То есть одна директория - один обьект (из файлов в этой директории)
___________________
 * Напишите код, который превратит содержимое папок в обьекты класса "Factory", учитывая аннотации над полями.
 * Парсинг должен учитывать следующие моменты:
 * 1. Парсятся значения только известных полей (которые есть в классе или упомянуты в аннотациях), все остальное игнорируется
 * 2. "null" должен распознаваться как null
 * 3. Ограничения аннотаций должны обрабатываться с помощью рефлексии
 * 4. Если файлы не прошли проверку, должна формироваться ошибка с информативным содержанием (см. ниже)
___________________
 Обработка ошибок:
 Если при попытке парсинга файла возникли ошибки проверки (не соблюдаются ограничения аннотаций), должно выбрасываться FactoryParsingException.
 Внутри должны быть перечислены все поля, при обработке которых возникли ошибки, и описание ошибки (напр. "Поле не может быть пустым")
 См. класс "FactoryParsingException" для доп. информации.
___________________
Обработка аннотаций и формирование ошибок оцениваются отдельно.
 * */
public class MainClass {

    private FactoryParsingServiceImpl factoryService = new FactoryParsingServiceImpl();

    public static void main(String[] args) throws Exception {
        new MainClass().run("src/itis/parsing2/resources/2");
    }

    private void run(String factoryDirectoryPath) throws Exception {

        Factory factory = null;

        try {
            factory = factoryService.parseFactoryData(factoryDirectoryPath);
        } catch (FactoryParsingException parsingException) {
            System.out.println(parsingException.getMessage());
            parsingException.getValidationErrors().forEach(factoryValidationError -> {
                System.out.printf("В поле %S найдена ошибка - %s\n", factoryValidationError.fieldName,
                        factoryValidationError.validationError);
            });

        } catch (RuntimeException e) {
            e.printStackTrace();
        }

        System.out.println(factory.toString());

    }

}










