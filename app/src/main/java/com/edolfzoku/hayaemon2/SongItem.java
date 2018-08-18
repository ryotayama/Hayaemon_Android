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

public class SongItem {
    private String strNumber = null;
    private String strTitle = null;
    private String strArtist = null;
    private String strPath = null;

    public void setNumber(String strNumber) { this.strNumber = strNumber; }
    public String getNumber() { return strNumber; }
    public void setTitle(String strTitle) { this.strTitle = strTitle; }
    public String getTitle() { return strTitle; }
    public void setArtist(String strArtist) { this.strArtist = strArtist; }
    public String getArtist() { return strArtist; }
    public void setPath(String strPath) { this.strPath = strPath; }
    public String getPath() { return strPath; }

    public SongItem() {};

    public SongItem(String strNumber, String strTitle, String strArtist, String strPath)
    {
        this.strNumber = strNumber;
        this.strTitle = strTitle;
        this.strArtist = strArtist;
        this.strPath = strPath;
    }
}
