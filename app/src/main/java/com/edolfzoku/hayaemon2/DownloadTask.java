package com.edolfzoku.hayaemon2;

import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

class DownloadTask extends AsyncTask<Integer, Integer, Integer>
{
    private final PlaylistFragment playlistFragment;
    private final URL url;
    private final String strPathTo;
    private final AlertDialog alert;

    DownloadTask(PlaylistFragment playlistFragment, URL url, String strPathTo, AlertDialog alert)
    {
        this.playlistFragment = playlistFragment;
        this.url = url;
        this.strPathTo = strPathTo;
        this.alert = alert;
    }

    @Override
    protected Integer doInBackground(Integer... params)
    {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            final int status = connection.getResponseCode();
            if (status == HttpURLConnection.HTTP_OK) {
                final InputStream input = connection.getInputStream();
                final DataInputStream dataInput = new DataInputStream(input);
                final FileOutputStream fileOutput = new FileOutputStream(strPathTo);
                final DataOutputStream dataOut = new DataOutputStream(fileOutput);
                final byte[] buffer = new byte[4096];
                int readByte;
                int totalReatByte = 0;
                int contentLength = connection.getContentLength();

                while((readByte = dataInput.read(buffer)) != -1)
                {
                    if(playlistFragment.isFinish()) break;
                    totalReatByte += readByte;
                    if(contentLength != 0)
                        publishProgress((int)(totalReatByte / contentLength * 100.0));
                    dataOut.write(buffer, 0, readByte);
                }
                dataInput.close();
                fileOutput.close();
                dataInput.close();
                input.close();
            }
            else
            {
                connection.disconnect();
                return 1;
            }
        }
        catch (IOException e)
        {
            if(connection != null) connection.disconnect();
            return 1;
        }
        finally
        {
            if(connection != null) connection.disconnect();
        }
        return 0;
    }

    @Override
    protected void onProgressUpdate(Integer... progress)
    {
        playlistFragment.setProgress(progress[0]);
    }

    @Override
    protected void onPostExecute(Integer integer)
    {
        playlistFragment.finishAddURL(strPathTo, alert, integer);
    }
}
