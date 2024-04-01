/*
 * Copyright (C) 2012 Paul Burke
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ipaulpro.afilechooser;

import static net.kdt.pojavlaunch.PojavZHTools.getIcon;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.kdt.pojavlaunch.R;

import java.io.File;

public class MouseFileListAdapter extends FileListAdapter {
    private final static int ICON_FILE = R.drawable.ic_file;
    private final LayoutInflater mInflater;

    public MouseFileListAdapter(Context context) {
        super(context);
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;

        if (row == null)
            row = mInflater.inflate(android.R.layout.simple_list_item_1, parent, false);

        TextView view = (TextView) row;

        // Get the file at the current position
        final File file = getItem(position);

        // Set the TextView as the file name
        view.setText(file.getName());

        if (file.getName().endsWith(".png")) {
            try {
                Drawable icon = getIcon(file.getAbsolutePath(), view.getContext());
                float density = view.getContext().getResources().getDisplayMetrics().density;
                int sizeInPx = Math.round(24 * density); //24dp

                icon.setBounds(0, 0, sizeInPx, sizeInPx);
                view.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);
            } catch (Exception e) {
                view.setCompoundDrawablesWithIntrinsicBounds(ICON_FILE, 0, 0, 0);
            }
        } else {
            view.setCompoundDrawablesWithIntrinsicBounds(ICON_FILE, 0, 0, 0);
        }
        view.setCompoundDrawablePadding(20);
        return row;
    }
}
