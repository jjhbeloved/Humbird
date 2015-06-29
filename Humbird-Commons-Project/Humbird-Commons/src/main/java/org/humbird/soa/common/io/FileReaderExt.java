package org.humbird.soa.common.io;

import java.io.*;
import java.util.Iterator;
import java.util.List;

/**
 * Created by david on 14-7-25.
 * 快速遍历文件, 可匹配文件内容
 */
public class FileReaderExt implements Iterable<List<String>> {

    private BufferedReaderExt bufferedReader;

    public FileReaderExt(String name) throws FileNotFoundException {
        this(new FileReader(name));
    }

    public FileReaderExt(FileReader fileReader) {
        this.bufferedReader = new BufferedReaderExt(fileReader);
    }

    public FileReaderExt(String name, String encoding) throws FileNotFoundException, UnsupportedEncodingException {
        this(name, encoding, "\n");
    }

    public FileReaderExt(String name, String encoding, String enter) throws FileNotFoundException, UnsupportedEncodingException {
        this(new File(name), encoding, enter);
    }

    public FileReaderExt(File file, String encoding, String enter) throws FileNotFoundException, UnsupportedEncodingException {
        this(file, encoding, enter, ",");
    }

    public FileReaderExt(File file, String encoding, String enter, String split) throws FileNotFoundException, UnsupportedEncodingException {
        this.bufferedReader = new BufferedReaderExt(new InputStreamReader(new FileInputStream(file), encoding), enter.charAt(0), split);
    }

    /**
     * 查找文件是否存在匹配name的字段
     *
     * @param name
     * @return 返回查找到的字段
     * @throws java.io.IOException 可能其他原因造成文件丢失, 不代表不存在
     */
    public String find(String name) throws IOException {
        while (true) {
            String str = bufferedReader.find(name);
            if (str == null) break;
            else if (str.length() == 0) continue;
            else return str;
        }
        return null;
    }

    public void close() throws IOException {
        bufferedReader.close();
    }

    @Override
    public Iterator<List<String>> iterator() {
        return new FileIterator();
    }

    private class FileIterator implements Iterator<List<String>> {

        private List<String> currentLines;

        @Override
        public boolean hasNext() {
            try {
                currentLines = bufferedReader.readLines();
            } catch (IOException e) {
                currentLines = null;
            }
            return currentLines != null;
        }

        @Override
        public List<String> next() {
            return currentLines;
        }

        @Override
        public void remove() {

        }
    }
}
