package itis.parsing2;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

interface FactoryParsingService {

    Factory parseFactoryData(String factoryDataDirectoryPath) throws FactoryParsingException, IOException, NoSuchMethodException, IllegalAccessException, InstantiationException, InvocationTargetException;

}
