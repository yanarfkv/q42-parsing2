package itis.parsing2;

import itis.parsing2.annotations.NotBlank;
import itis.parsing2.annotations.Concatenate;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static itis.parsing2.FactoryParsingException.*;

public class FactoryParsingServiceImpl implements FactoryParsingService {

    @Override
    public Factory parseFactoryData(String factoryDataDirectoryPath) throws FactoryParsingException, IOException, NoSuchMethodException, IllegalAccessException, InstantiationException, InvocationTargetException {
        List<File> filesInDirectory = Files.walk(Paths.get(factoryDataDirectoryPath))
                .filter(Files::isRegularFile)
                .map(Path::toFile)
                .collect(Collectors.toList());
        HashMap<String, String> factoryFields = new HashMap<>();
        for (File file : filesInDirectory
        ) {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String str = "";
            reader.readLine();
            while (!(str = reader.readLine()).equals("---")) {
                List<String> collect = Arrays.stream(str.split(":"))
                        .map(s -> s.replace("\"", "").trim())
                        .collect(Collectors.toList());
                if (collect.size() > 1)
                    factoryFields.put(collect.get(0), collect.get(1));
            }
        }
        Class<Factory> factoryClass = Factory.class;
        List<FactoryValidationError> errors = new ArrayList<>();

        HashMap<String, String> parsedFactoryFields = factoryFields;
        Constructor<Factory> factoryConstructor = factoryClass.getDeclaredConstructor();
        factoryConstructor.setAccessible(true);
        Object factory = factoryConstructor.newInstance();
        Factory resultFactory = (Factory) factory;
        for (Field field : factory.getClass().getDeclaredFields()
        ) {
            boolean isProblem = false;
            field.setAccessible(true);
            if (field.getAnnotation(NotBlank.class) != null) {
                String fieldValue = parsedFactoryFields.get(field.getName());
                if (fieldValue == null || fieldValue.equals("") || fieldValue.equals("null")) {
                    errors.add(new FactoryValidationError(field.getName(),
                            "ожидалось непустое значение поля " + field.getName()));
                    isProblem = true;
                }
            }
            if (field.getAnnotation(Concatenate.class) != null) {
                Concatenate annotation = field.getAnnotation(Concatenate.class);
                String[] fieldNames = annotation.fieldNames();
                String firstName = fieldNames[0];
                String secondName = fieldNames[1];
                String middleName = fieldNames[2];
                String delimiter = annotation.delimiter();
                if (parsedFactoryFields.containsKey(firstName) && parsedFactoryFields.containsKey(secondName)
                        && parsedFactoryFields.containsKey(middleName)) {
                    parsedFactoryFields.put(field.getName(),
                            parsedFactoryFields.get(firstName)
                                    + delimiter + parsedFactoryFields.get(secondName)
                                    + delimiter + parsedFactoryFields.get(middleName));
                } else {
                    errors.add(new FactoryValidationError(field.getName(),
                            "не найдены поля, из которых нужно брать значения"));
                    isProblem = true;
                }
            }

            if (!isProblem && parsedFactoryFields.containsKey(field.getName())) {
                if (field.getType().equals(String.class)) {
                    try {
                        field.set(resultFactory, parsedFactoryFields.get(field.getName()));
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
                if (field.getType().equals(Long.class)) {
                    try {
                        field.set(resultFactory, Long.parseLong(parsedFactoryFields.get(field.getName())));
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
                if (field.getType().equals(List.class)) {
                    List<String> list = Arrays.asList(parsedFactoryFields.get(field.getName())
                            .replace("[", "")
                            .replace("]", "")
                            .split(","));
                    try {
                        field.set(resultFactory, list);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }

                }
            }
        }
        if (!errors.isEmpty()) {
            throw new FactoryParsingException("Найдены следующие ошибки:", errors);
        }
        return (Factory) factory;
    }
}
