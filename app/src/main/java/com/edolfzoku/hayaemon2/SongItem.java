/*
 * SongItem
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

class SongItem {
    private boolean bSelected = false;
    private String strNumber;
    private String strTitle;
    private String strArtist;
    private final String strPath;
    private float fPeak;
    private String strPathArtwork;
    private String strTime;

    void setSelected(boolean bSelected) { this.bSelected = bSelected; }
    boolean isSelected() { return bSelected; }
    void setNumber(String strNumber) { this.strNumber = strNumber; }
    String getNumber() { return strNumber; }
    public void setTitle(String strTitle) { this.strTitle = strTitle; }
    String getTitle() { return strTitle; }
    void setArtist(String strArtist) { this.strArtist = strArtist; }
    String getArtist() { return strArtist; }
    public String getPath() { return strPath; }
    void setPeak(float fPeak) { this.fPeak = fPeak; }
    float getPeak() { return fPeak; }
    void setPathArtwork(String strPathArtwork) { this.strPathArtwork = strPathArtwork; }
    String getPathArtwork() { return strPathArtwork; }
    void setTime(String strTime) { this.strTime = strTime; }
    String getTime() { return strTime; }

    SongItem(String strNumber, String strTitle, String strArtist, String strPath)
    {
        this.strNumber = strNumber;
        this.strTitle = strTitle;
        this.strArtist = strArtist;
        this.strPath = strPath;
        this.fPeak = 0.0f;
    }
}
