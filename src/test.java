import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipException;

import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;

/* 
 * Copyright (c) 2016, S.F. Express Inc. All rights reserved.
 */

/**
 * 描述：
 * 
 * <pre>
 * HISTORY
 * ****************************************************************************
 *  ID   DATE           PERSON          REASON
 *  1    2016年1月27日      Steven.Zhu         Create
 * ****************************************************************************
 * </pre>
 * 
 * @author Steven.Zhu
 * @since
 */

public class test {

    public static void readByApacheZipFile(String archive, String decompressDir)  
            throws IOException, FileNotFoundException, ZipException {  
        
        BufferedInputStream bi;  
        
        ZipFile zf = new ZipFile(archive, "GBK");//支持中文   
  
        Enumeration e = zf.getEntries();  
        while (e.hasMoreElements()) {  
            ZipEntry ze2 = (ZipEntry) e.nextElement();  
            String entryName = ze2.getName();  
            String path = decompressDir + "/" + entryName;  
            if (ze2.isDirectory()) {  
                System.out.println("正在创建解压目录 - " + entryName);  
                File decompressDirFile = new File(path);  
                if (!decompressDirFile.exists()) {  
                    decompressDirFile.mkdirs();  
                }  
            } else {  
                System.out.println("正在创建解压文件 - " + entryName);  
                String fileDir = path.substring(0, path.lastIndexOf("/"));  
                File fileDirFile = new File(fileDir);  
                if (!fileDirFile.exists()) {  
                    fileDirFile.mkdirs();  
                }  
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(  
                        decompressDir + "/" + entryName));  
  
                bi = new BufferedInputStream(zf.getInputStream(ze2));  
                byte[] readContent = new byte[1024];  
                int readCount = bi.read(readContent);  
                while (readCount != -1) {  
                    bos.write(readContent, 0, readCount);  
                    readCount = bi.read(readContent);  
                }  
                bos.close();  
            }  
        }  
        zf.close();   
    }
    
    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.err.println("args length = 2");
            System.exit(-1);
        }
        
//        args[0] = "E:/tmp/中文测试.zip";
//        args[1] = "E:/tmp/zip/";
        
        readByApacheZipFile(args[0], args[1]);
    }
}
