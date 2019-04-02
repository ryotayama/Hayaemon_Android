/*
 * EffectItem
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

public class EffectItem {
    private String strEffectName = null;
    private boolean bSelected = false;
    private boolean bEditEnabled = false;

    void setEffectName(String strEffectName) { this.strEffectName = strEffectName; }
    String getEffectName() { return strEffectName; }
    void setSelected(boolean bSelected) { this.bSelected = bSelected; }
    public boolean isSelected() { return bSelected; }
    void setbEditEnabled(boolean bEditEnabled) { this.bEditEnabled = bEditEnabled; }
    boolean isEditEnabled() { return bEditEnabled; }

    EffectItem() {}

    EffectItem(String strEffectName, boolean bEditEnabled)
    {
        this.strEffectName = strEffectName;
        this.bEditEnabled = bEditEnabled;
    }
}
