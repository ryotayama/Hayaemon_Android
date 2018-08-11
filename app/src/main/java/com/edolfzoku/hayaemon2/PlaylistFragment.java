/*
 * PlaylistFragment
 *
 * Copyright (c) 2018 Ryota Yamauchi. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.edolfzoku.hayaemon2;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.google.gson.Gson;
import com.un4seen.bass.BASS;
import com.un4seen.bass.BASS_AAC;
import com.un4seen.bass.BASS_FX;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static android.app.Activity.RESULT_OK;
import static com.un4seen.bass.BASS_AAC.BASS_AAC_StreamCreateFileUser;

public class PlaylistFragment extends Fragment implements AdapterView.OnItemClickListener, View.OnClickListener {
    private ArrayList<PlaylistItem> listSongs;
    private int hFx20K, hFx16K, hFx12_5K, hFx10K, hFx8K, hFx6_3K, hFx5K, hFx4K, hFx3_15K, hFx2_5K, hFx2K, hFx1_6K, hFx1_25K, hFx1K, hFx800, hFx630, hFx500, hFx400, hFx315, hFx250, hFx200, hFx160, hFx125, hFx100, hFx80, hFx63, hFx50, hFx40, hFx31_5, hFx25, hFx20;
    private List<String> arSongsPath;
    private List<Boolean> arPlayed;
    private ListView listView;
    private PlaylistAdapter adapter;
    private MainActivity activity;
    private int nPlaying;
    private int nDeleteItem;

    public int getPlaying() { return nPlaying; }

    public PlaylistFragment()
    {
        activity = null;
        nPlaying = -1;
        listSongs = new ArrayList<>();
        arSongsPath = new ArrayList<>();
        arPlayed = new ArrayList<>();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context != null && context instanceof MainActivity) {
            activity = (MainActivity) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        activity = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        adapter = new PlaylistAdapter(activity, R.layout.playlist_item, listSongs);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.btnRewind)
        {
            if(activity.hStream == 0) return;
            if(BASS.BASS_ChannelBytes2Seconds(activity.hStream, BASS.BASS_ChannelGetPosition(activity.hStream, BASS.BASS_POS_BYTE)) > activity.dLoopA + 1.0)
                BASS.BASS_ChannelSetPosition(activity.hStream, BASS.BASS_ChannelSeconds2Bytes(activity.hStream, activity.dLoopA), BASS.BASS_POS_BYTE);
            else
                playPrev();
        }
        else if(v.getId() == R.id.btnStop)
        {
            if(activity.hStream == 0) return;
            stop();
        }
        else if(v.getId() == R.id.btnPlay)
        {
            if(BASS.BASS_ChannelIsActive(activity.hStream) == BASS.BASS_ACTIVE_PLAYING)
            {
                pause();
            }
            else
            {
                if(BASS.BASS_ChannelIsActive(activity.hStream) == BASS.BASS_ACTIVE_PAUSED)
                {
                    play();
                }
                else
                {
                    if(activity.hStream == 0)
                    {
                        playNext();
                    }
                }
            }
        }
        else if(v.getId() == R.id.btnForward)
        {
            if(activity.hStream == 0) return;
            playNext();
        }
        else if(v.getId() == R.id.btnPlayMode)
        {
            Button btnPlayMode = (Button)activity.findViewById(R.id.btnPlayMode);
            if(btnPlayMode.getText().equals("通常モード"))
            {
                btnPlayMode.setText("１曲ループ");
                btnPlayMode.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_sloop, 0, 0);
            }
            else if(btnPlayMode.getText().equals("１曲ループ"))
            {
                btnPlayMode.setText("全曲ループ");
                btnPlayMode.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_aloop, 0, 0);
            }
            else if(btnPlayMode.getText().equals("全曲ループ"))
            {
                btnPlayMode.setText("ランダム再生");
                btnPlayMode.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_random, 0, 0);
            }
            else if(btnPlayMode.getText().equals("ランダム再生"))
            {
                btnPlayMode.setText("通常モード");
                btnPlayMode.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_normal, 0, 0);
            }

            SharedPreferences preferences = activity.getSharedPreferences("SaveData", Activity.MODE_PRIVATE);
            int nPlayMode = 0;
            if(btnPlayMode.getText().equals("通常モード"))
                nPlayMode = 0;
            else if(btnPlayMode.getText().equals("１曲ループ"))
                nPlayMode = 1;
            else if(btnPlayMode.getText().equals("全曲ループ"))
                nPlayMode = 2;
            else if(btnPlayMode.getText().equals("ランダム再生"))
                nPlayMode = 3;
            preferences.edit().putInt("playmode", nPlayMode).commit();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.fragment_playlist, container, false);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        listView = (ListView)activity.findViewById(R.id.listView);

        listView.setAdapter(adapter);

        listView.setOnItemClickListener(this);

        registerForContextMenu(listView);

        Button btnRewind = (Button) activity.findViewById(R.id.btnRewind);
        btnRewind.setOnClickListener(this);

        Button btnPlay = (Button) activity.findViewById(R.id.btnPlay);
        btnPlay.setOnClickListener(this);

        Button btnStop = (Button) activity.findViewById(R.id.btnStop);
        btnStop.setOnClickListener(this);

        Button btnForward = (Button) activity.findViewById(R.id.btnForward);
        btnForward.setOnClickListener(this);

        Button btnPlayMode = (Button) activity.findViewById(R.id.btnPlayMode);
        btnPlayMode.setOnClickListener(this);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 1)
        {
            if(resultCode == RESULT_OK)
            {
                if(Build.VERSION.SDK_INT < 19)
                {
                    addSong(activity, data.getData());
                }
                else
                {
                    if(data.getClipData() == null)
                    {
                        addSong(activity, data.getData());
                    }
                    else
                    {
                        for(int i = 0; i < data.getClipData().getItemCount(); i++)
                        {
                            Uri uri = data.getClipData().getItemAt(i).getUri();
                            addSong(activity, uri);
                        }
                    }
                }
                adapter.notifyDataSetChanged();
            }
        }

        SharedPreferences preferences = activity.getSharedPreferences("SaveData", Activity.MODE_PRIVATE);
        Gson gson = new Gson();
        preferences.edit().putString("arSongsPath", gson.toJson(arSongsPath)).commit();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo info)
    {
        super.onCreateContextMenu(menu, view, info);
        if(listView.equals(view))
        {
            AdapterView.AdapterContextMenuInfo adapterInfo = (AdapterView.AdapterContextMenuInfo)info;
            ListView listView = (ListView)view;
            nDeleteItem = adapterInfo.position;
            PlaylistItem item = (PlaylistItem)listView.getItemAtPosition(nDeleteItem);
            String strSong = item.getTitle();

            menu.setHeaderTitle(strSong);
            menu.add("削除");
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item)
    {
        if(item.getTitle().equals("削除"))
        {
            if(nDeleteItem < nPlaying) nPlaying--;

            listSongs.remove(nDeleteItem);
            arSongsPath.remove(nDeleteItem);
            arPlayed.remove(nDeleteItem);

            for(int i = nDeleteItem; i < listSongs.size(); i++) {
                PlaylistItem playlistItem = listSongs.get(i);
                playlistItem.setNumber(String.format("%d", i+1));
            }

            adapter.notifyDataSetChanged();

            SharedPreferences preferences = activity.getSharedPreferences("SaveData", Activity.MODE_PRIVATE);
            Gson gson = new Gson();
            preferences.edit().putString("arSongsPath", gson.toJson(arSongsPath)).commit();
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View v, int position, long id)
    {
        playSong(position);
    }

    public void play()
    {
        if(MainActivity.hStream == 0) return;
        BASS.BASS_ChannelPlay(MainActivity.hStream, false);
        Button btnPlay = (Button)getActivity().findViewById(R.id.btnPlay);
        btnPlay.setBackgroundColor(Color.argb(0, 0, 0, 0));
        adapter.notifyDataSetChanged();
    }

    public void pause()
    {
        if(MainActivity.hStream == 0) return;
        BASS.BASS_ChannelPause(MainActivity.hStream);
        Button btnPlay = (Button)getActivity().findViewById(R.id.btnPlay);
        btnPlay.setBackgroundColor(Color.argb(64, 0, 0, 0));
        adapter.notifyDataSetChanged();
    }

    public void playPrev()
    {
        if(MainActivity.hStream == 0) return;
        nPlaying--;
        if(nPlaying < 0) return;
        playSong(nPlaying);
    }

    public void playNext()
    {
        MainActivity activity = (MainActivity)getActivity();
        Button btnPlayMode = (Button)activity.findViewById(R.id.btnPlayMode);
        if(btnPlayMode.getText().equals("通常モード") || btnPlayMode.getText().equals("１曲ループ"))
        {
            nPlaying++;
            if(nPlaying >= listSongs.size())
            {
                stop();
                return;
            }
        }
        else if(btnPlayMode.getText().equals("全曲ループ"))
        {
            nPlaying++;
            if(nPlaying >= listSongs.size())
            {
                nPlaying = 0;
            }
        }
        else if(btnPlayMode.getText().equals("ランダム再生"))
        {
            ArrayList<Integer> arTemp = new ArrayList<Integer>();
            for(int i = 0; i < arPlayed.size(); i++)
            {
                if(i == nPlaying) continue;
                Boolean bPlayed = arPlayed.get(i);
                if(!bPlayed.booleanValue())
                {
                    arTemp.add(i);
                }
            }
            if(arTemp.size() == 0)
            {
                for(int i = 0; i < arPlayed.size(); i++)
                {
                    arPlayed.set(i, false);
                }
            }
            if(arPlayed.size() != 1)
            {
                Random random = new Random();
                if(arTemp.size() == 0 || arTemp.size() == arPlayed.size())
                {
                    nPlaying = random.nextInt(arPlayed.size());
                }
                else
                {
                    int nRandom = random.nextInt(arTemp.size());
                    nPlaying = arTemp.get(nRandom);
                }
            }
        }
        playSong(nPlaying);
    }

    public void playSong(int nSong)
    {
        MainActivity activity = (MainActivity)getActivity();
        activity.clearLoop();
        nPlaying = nSong;
        String strPath = arSongsPath.get(nSong);
        if(MainActivity.hStream != 0)
        {
            BASS.BASS_StreamFree(MainActivity.hStream);
            MainActivity.hStream = 0;
        }
        arPlayed.set(nSong, true);

        BASS.BASS_FILEPROCS fileprocs=new BASS.BASS_FILEPROCS() {
            @Override
            public boolean FILESEEKPROC(long offset, Object user) {
                FileChannel fc=(FileChannel)user;
                try {
                    fc.position(offset);
                    return true;
                } catch (IOException e) {
                }
                return false;
            }

            @Override
            public int FILEREADPROC(ByteBuffer buffer, int length, Object user) {
                FileChannel fc=(FileChannel)user;
                try {
                    return fc.read(buffer);
                } catch (IOException e) {
                }
                return 0;
            }

            @Override
            public long FILELENPROC(Object user) {
                FileChannel fc=(FileChannel)user;
                try {
                    return fc.size();
                } catch (IOException e) {
                }
                return 0;
            }

            @Override
            public void FILECLOSEPROC(Object user) {
                FileChannel fc=(FileChannel)user;
                try {
                    fc.close();
                } catch (IOException e) {
                }
            }
        };

        Uri uri = Uri.parse(strPath);
        if(uri.getScheme() != null && uri.getScheme().equals("content")) {
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            boolean bError = false;
            try {
                mmr.setDataSource(activity.getApplicationContext(), Uri.parse(strPath));
            }
            catch(Exception e) {
                bError = true;
            }
            String strMimeType = null;
            if(!bError)
                strMimeType = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE);
            ContentResolver cr = activity.getApplicationContext().getContentResolver();
            try {
                AssetFileDescriptor afd = cr.openAssetFileDescriptor(Uri.parse(strPath), "r");
                if(afd == null) return;
                FileChannel fc = afd.createInputStream().getChannel();
                if(strMimeType == "audio/mp4")
                    MainActivity.hStream = BASS_AAC.BASS_AAC_StreamCreateFileUser(BASS.STREAMFILE_NOBUFFER, BASS.BASS_STREAM_DECODE, fileprocs, fc);
                else
                    MainActivity.hStream = BASS.BASS_StreamCreateFileUser(BASS.STREAMFILE_NOBUFFER, BASS.BASS_STREAM_DECODE, fileprocs, fc);
            } catch (IOException e) {
                return;
            }
        }
        else {
            MainActivity.hStream = BASS.BASS_StreamCreateFile(strPath, 0, 0, BASS.BASS_STREAM_DECODE);
        }
        if(MainActivity.hStream == 0) return;

        MainActivity.hStream = BASS_FX.BASS_FX_ReverseCreate(MainActivity.hStream, 2, BASS.BASS_STREAM_DECODE | BASS_FX.BASS_FX_FREESOURCE);
        MainActivity.hStream = BASS_FX.BASS_FX_TempoCreate(MainActivity.hStream, BASS_FX.BASS_FX_FREESOURCE);
        int chan = BASS_FX.BASS_FX_TempoGetSource(MainActivity.hStream);
        BASS.BASS_ChannelSetAttribute(chan, BASS_FX.BASS_ATTRIB_REVERSE_DIR, BASS_FX.BASS_FX_RVS_FORWARD);
        MainActivity.hFxVol = BASS.BASS_ChannelSetFX(MainActivity.hStream, BASS_FX.BASS_FX_BFX_VOLUME, 0);
        hFx20K = BASS.BASS_ChannelSetFX(MainActivity.hStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
        hFx16K = BASS.BASS_ChannelSetFX(MainActivity.hStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
        hFx12_5K = BASS.BASS_ChannelSetFX(MainActivity.hStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
        hFx10K = BASS.BASS_ChannelSetFX(MainActivity.hStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
        hFx8K = BASS.BASS_ChannelSetFX(MainActivity.hStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
        hFx6_3K = BASS.BASS_ChannelSetFX(MainActivity.hStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
        hFx5K = BASS.BASS_ChannelSetFX(MainActivity.hStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
        hFx4K = BASS.BASS_ChannelSetFX(MainActivity.hStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
        hFx3_15K = BASS.BASS_ChannelSetFX(MainActivity.hStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
        hFx2_5K = BASS.BASS_ChannelSetFX(MainActivity.hStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
        hFx2K = BASS.BASS_ChannelSetFX(MainActivity.hStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
        hFx1_6K = BASS.BASS_ChannelSetFX(MainActivity.hStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
        hFx1_25K = BASS.BASS_ChannelSetFX(MainActivity.hStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
        hFx1K = BASS.BASS_ChannelSetFX(MainActivity.hStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
        hFx800 = BASS.BASS_ChannelSetFX(MainActivity.hStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
        hFx630 = BASS.BASS_ChannelSetFX(MainActivity.hStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
        hFx500 = BASS.BASS_ChannelSetFX(MainActivity.hStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
        hFx400 = BASS.BASS_ChannelSetFX(MainActivity.hStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
        hFx315 = BASS.BASS_ChannelSetFX(MainActivity.hStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
        hFx250 = BASS.BASS_ChannelSetFX(MainActivity.hStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
        hFx200 = BASS.BASS_ChannelSetFX(MainActivity.hStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
        hFx160 = BASS.BASS_ChannelSetFX(MainActivity.hStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
        hFx125 = BASS.BASS_ChannelSetFX(MainActivity.hStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
        hFx100 = BASS.BASS_ChannelSetFX(MainActivity.hStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
        hFx80 = BASS.BASS_ChannelSetFX(MainActivity.hStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
        hFx63 = BASS.BASS_ChannelSetFX(MainActivity.hStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
        hFx50 = BASS.BASS_ChannelSetFX(MainActivity.hStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
        hFx40 = BASS.BASS_ChannelSetFX(MainActivity.hStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
        hFx31_5 = BASS.BASS_ChannelSetFX(MainActivity.hStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
        hFx25 = BASS.BASS_ChannelSetFX(MainActivity.hStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
        hFx20 = BASS.BASS_ChannelSetFX(MainActivity.hStream, BASS_FX.BASS_FX_BFX_PEAKEQ, 1);
        ControlFragment controlFragment = (ControlFragment)activity.mSectionsPagerAdapter.getItem(1);
        BASS.BASS_ChannelSetAttribute(MainActivity.hStream, BASS_FX.BASS_ATTRIB_TEMPO, controlFragment.fSpeed);
        BASS.BASS_ChannelSetAttribute(MainActivity.hStream, BASS_FX.BASS_ATTRIB_TEMPO_PITCH, controlFragment.fPitch);
        EqualizerFragment equalizerFragment = (EqualizerFragment)activity.mSectionsPagerAdapter.getItem(3);
        equalizerFragment.setArHFX(new int[] {hFx20K, hFx16K, hFx12_5K, hFx10K, hFx8K, hFx6_3K, hFx5K, hFx4K, hFx3_15K, hFx2_5K, hFx2K, hFx1_6K, hFx1_25K, hFx1K, hFx800, hFx630, hFx500, hFx400, hFx315, hFx250, hFx200, hFx160, hFx125, hFx100, hFx80, hFx63, hFx50, hFx40, hFx31_5, hFx25, hFx20});
        equalizerFragment.setEQ();
        EffectFragment effectFragment = (EffectFragment)activity.mSectionsPagerAdapter.getItem(4);
        effectFragment.applyEffect();
        activity.setSync();
        BASS.BASS_ChannelPlay(MainActivity.hStream, false);
        Button btnPlay = (Button)getActivity().findViewById(R.id.btnPlay);
        btnPlay.setText("一時停止");
        btnPlay.setCompoundDrawablesWithIntrinsicBounds(0,R.drawable.ic_pause,0,0);
        LoopFragment loopFragment = (LoopFragment)activity.mSectionsPagerAdapter.getItem(2);
        loopFragment.drawWaveForm(strPath);
        adapter.notifyDataSetChanged();
    }

    public void stop()
    {
        if(MainActivity.hStream == 0) return;
        nPlaying = -1;
        BASS.BASS_ChannelStop(MainActivity.hStream);
        MainActivity.hStream = 0;
        Button btnPlay = (Button)getActivity().findViewById(R.id.btnPlay);
        btnPlay.setText("再生");
        btnPlay.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_play, 0, 0);
        MainActivity activity = (MainActivity)getActivity();
        activity.clearLoop();
        adapter.notifyDataSetChanged();
    }

    public void addSong(MainActivity activity, Uri uri)
    {
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        boolean bError = false;
        try {
            mmr.setDataSource(activity.getApplicationContext(), uri);
        }
        catch(Exception e) {
            bError = true;
        }
        String strTitle = null;
        String strArtist = null;
        if(!bError) {
            strTitle = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            strArtist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
        }
        if(strTitle != null) {
            PlaylistItem item = new PlaylistItem(String.format("%d", listSongs.size()+1), strTitle, strArtist);
            listSongs.add(item);
        }
        else
        {
            strTitle = getFileNameFromUri(activity.getApplicationContext(), uri);
            if(strTitle == null) {
                int startIndex = uri.toString().lastIndexOf('/');
                strTitle = uri.toString().substring(startIndex + 1);
            }
            PlaylistItem item = new PlaylistItem(String.format("%d", listSongs.size()+1), strTitle, "");
            listSongs.add(item);
        }
        arSongsPath.add(uri.toString());
        arPlayed.add(false);
    }

    public String getFileNameFromUri(Context context, Uri uri) {
        if (null == uri) return null;

        String scheme = uri.getScheme();

        String fileName = null;
        switch (scheme) {
            case "content":
                String[] projection = {MediaStore.MediaColumns.DISPLAY_NAME};
                Cursor cursor = null;
                try {
                    cursor = context.getContentResolver()
                            .query(uri, projection, null, null, null);
                }
                catch(Exception e) {
                    return null;
                }
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        fileName = cursor.getString(
                                cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME));
                    }
                    cursor.close();
                }
                break;

            case "file":
                fileName = new File(uri.getPath()).getName();
                break;

            default:
                break;
        }
        return fileName;
    }
}
