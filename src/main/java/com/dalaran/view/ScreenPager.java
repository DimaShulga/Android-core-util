/*
 * Copyright 2014 Dima Shulga
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dalaran.view;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class ScreenPager extends ViewPager {
    private PagerAdapter adapter = new ScreenPagerAdapter();
    private List<View> viewList = new ArrayList<>();

    public ScreenPager(Context context) {
        super(context);
        init();
    }

    public ScreenPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @Override
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    }

    private void init() {
        setAdapter(adapter);
    }

    @Override
    public void addView(View child, ViewGroup.LayoutParams params) {
        addScreen(child);
    }

    public void addScreen(View screen) {
        viewList.add(screen);
        adapter.notifyDataSetChanged();
    }

    private class ScreenPagerAdapter extends PagerAdapter {
        @Override
        public int getCount() {
            return viewList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object o) {
            return view.equals(o);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view = viewList.get(position);
            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }

    /**
     * Use - in MainActivity.onCreate add:
     * LayoutInflater.from(this).setFactory(ScreenPager.getShortNameFactory());
     *
     * @return
     */
    public static LayoutInflater.Factory getShortNameFactory() {
        return new LayoutInflater.Factory() {
            @Override
            public View onCreateView(String name, Context context, AttributeSet attrs) {
                if (ScreenPager.class.getSimpleName().equals(name)) {
                    return new ScreenPager(context, attrs);
                }
                return null;
            }
        };
    }
}