package com.au.assure;

import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class C3S_Reader {

    private final long C3S_HEADER_LENGTH = (8l *1024l);

    byte [] m_buffer;
    byte [] m_Line = new byte[150];
    long m_lIndex;
    long m_lLength;

    public C3S_Reader(String strFileName)
    {
        MakeSaveDirectory();
        ReadFile( strFileName );
    }

    private static String m_strRoot;
    protected static void MakeSaveDirectory()
    {
        String root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toString();
        if( root != null) {
            m_strRoot = root + "/logs";
            File myDir = new File(m_strRoot);
            if (!myDir.exists())
                myDir.mkdirs();
        }
        else
            m_strRoot = "/sdcard";
    }

    private boolean ReadFile( String strFileName ) {
        FileInputStream fis = null;

        String strFilePath = m_strRoot + "/" + strFileName;

        File file = new File(strFilePath);
        m_lLength = file.length();
        try {
            file.createNewFile();

            fis = new FileInputStream(file);

            if( fis != null ) {
                int iLen = (int)m_lLength;
                m_buffer = new byte[iLen];

                int iRead = fis.read(m_buffer,0,iLen);

                m_lIndex = C3S_HEADER_LENGTH;

                fis.close();

                if( iRead>0 )
                {
                    return true;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    public byte [] GetLine()
    {
        if( m_lLength == 0 )
        {
            return null;
        }

        long lOldIndex = m_lIndex;
        int iL =  m_buffer[(int)m_lIndex];
        for( int i=0;i<iL;i++)
            m_Line[i] = m_buffer[(int)m_lIndex + i];

        m_lIndex += iL;

        if( ( m_lIndex >= m_lLength ) || (m_buffer[(int)m_lIndex]==0 ) )
            m_lIndex = C3S_HEADER_LENGTH;

        return m_Line;
    }

}
