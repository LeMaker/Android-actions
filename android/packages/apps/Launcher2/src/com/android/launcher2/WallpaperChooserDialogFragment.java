/*
 * Copyright (C) 2010 The Android Open Source Project
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
package com.android.launcher2;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.SpinnerAdapter;

import com.android.launcher.R;

import java.io.IOException;
import java.util.ArrayList;

import java.io.File;

import java.io.InputStream;
import java.io.FileInputStream;
public class WallpaperChooserDialogFragment extends DialogFragment implements
        AdapterView.OnItemSelectedListener, AdapterView.OnItemClickListener {

    private static final String TAG = "Launcher.WallpaperChooserDialogFragment";
    private static final String EMBEDDED_KEY = "com.android.launcher2."
            + "WallpaperChooserDialogFragment.EMBEDDED_KEY";
    private static final String CUSTOM_WALLPAPERPATH = "/system/etc/wallpaper";

    private boolean mEmbedded;
    private Bitmap mBitmap = null;

    private ArrayList<Integer> mThumbs;
    private ArrayList<Integer> mImages;
    private WallpaperLoader mLoader;
    private WallpaperDrawable mWallpaperDrawable = new WallpaperDrawable();
	
    private boolean mCustomWallpaperPath = false;

    public static WallpaperChooserDialogFragment newInstance() {
        WallpaperChooserDialogFragment fragment = new WallpaperChooserDialogFragment();
        fragment.setCancelable(true);
        return fragment;
    }

    /**
    ************************************
    *      
    *ActionsCode(author:lizihao, type:change_code)
    */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null && savedInstanceState.containsKey(EMBEDDED_KEY)) {
            mEmbedded = savedInstanceState.getBoolean(EMBEDDED_KEY);
        } else {
            mEmbedded = isInLayout();
        }
        // ActionsCode( lizihao, new feature)
        if(checkWallpaperPath()) {
            mCustomWallpaperPath = true;
        } else {
            mCustomWallpaperPath = false;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(EMBEDDED_KEY, mEmbedded);
    }
	
    /**
    ************************************
    *      
    *ActionsCode(lizihao, new_method))
    */
    public boolean checkWallpaperPath() {
        File mFile = new File(CUSTOM_WALLPAPERPATH);
            if(mFile.exists()){
                return true;
            } else {
                return false;
            }
    }

    private void cancelLoader() {
        if (mLoader != null && mLoader.getStatus() != WallpaperLoader.Status.FINISHED) {
            mLoader.cancel(true);
            mLoader = null;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        cancelLoader();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        cancelLoader();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        /* On orientation changes, the dialog is effectively "dismissed" so this is called
         * when the activity is no longer associated with this dying dialog fragment. We
         * should just safely ignore this case by checking if getActivity() returns null
         */
        Activity activity = getActivity();
        if (activity != null) {
            activity.finish();
        }
    }

    /* This will only be called when in XLarge mode, since this Fragment is invoked like
     * a dialog in that mode
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        findWallpapers();

        return null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        findWallpapers();

        /* If this fragment is embedded in the layout of this activity, then we should
         * generate a view to display. Otherwise, a dialog will be created in
         * onCreateDialog()
         */
        if (mEmbedded) {
            View view = inflater.inflate(R.layout.wallpaper_chooser, container, false);
            view.setBackground(mWallpaperDrawable);

            final Gallery gallery = (Gallery) view.findViewById(R.id.gallery);
            gallery.setCallbackDuringFling(false);
            gallery.setOnItemSelectedListener(this);
            gallery.setAdapter(new ImageAdapter(getActivity()));

            View setButton = view.findViewById(R.id.set);
            setButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectWallpaper(gallery.getSelectedItemPosition());
                }
            });
            return view;
        }
        return null;
    }

	
    /**
    ************************************
    *      
    *ActionsCode(lizihao, new_method))
    */
    private void selectWallpaper(int position) {
        // ActionsCode( lizihao, new feature)
        if(mCustomWallpaperPath) {
            SetWallpaper mSetWallpaper = new SetWallpaper(position);
    	    mSetWallpaper.start();
		    
            Activity activity = getActivity();
            activity.setResult(Activity.RESULT_OK);
            activity.finish();
        } else {
        try {
            WallpaperManager wpm = (WallpaperManager) getActivity().getSystemService(
                    Context.WALLPAPER_SERVICE);
            wpm.setResource(mImages.get(position));
            Activity activity = getActivity();
            activity.setResult(Activity.RESULT_OK);
            activity.finish();
        } catch (IOException e) {
            Log.e(TAG, "Failed to set wallpaper: " + e);
            }
        }
    }
	
    /**
    ************************************
    *      
    *ActionsCode(lizihao, new_method))
    */
    class SetWallpaper extends Thread {

        public int wallpaper_position;
		
    	public SetWallpaper(int position) {
            wallpaper_position = position;
        }

        @Override
        public void run() {
            try {
                WallpaperManager wpm = (WallpaperManager) getActivity().getSystemService(
                        Context.WALLPAPER_SERVICE);
				
                InputStream is = new FileInputStream(new File("/system/etc/wallpaper/wallpaper_0" + wallpaper_position + ".png"));
                wpm.setStream(is);
                is.close();
				
            } catch (IOException e) {
                Log.e(TAG, "Failed to set wallpaper: " + e);
            }
        }
    }

    // Click handler for the Dialog's GridView
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        selectWallpaper(position);
    }

    // Selection handler for the embedded Gallery view
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (mLoader != null && mLoader.getStatus() != WallpaperLoader.Status.FINISHED) {
            mLoader.cancel();
        }
        mLoader = (WallpaperLoader) new WallpaperLoader().execute(position);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    private void findWallpapers() {
        mThumbs = new ArrayList<Integer>(24);
        mImages = new ArrayList<Integer>(24);

        final Resources resources = getResources();
        // Context.getPackageName() may return the "original" package name,
        // com.android.launcher2; Resources needs the real package name,
        // com.android.launcher. So we ask Resources for what it thinks the
        // package name should be.
        final String packageName = resources.getResourcePackageName(R.array.wallpapers);

        addWallpapers(resources, packageName, R.array.wallpapers);
        addWallpapers(resources, packageName, R.array.extra_wallpapers);
    }

    private void addWallpapers(Resources resources, String packageName, int list) {
        final String[] extras = resources.getStringArray(list);
        for (String extra : extras) {
            int res = resources.getIdentifier(extra, "drawable", packageName);
            if (res != 0) {
                final int thumbRes = resources.getIdentifier(extra + "_small",
                        "drawable", packageName);

                if (thumbRes != 0) {
                    mThumbs.add(thumbRes);
                    mImages.add(res);
                    // Log.d(TAG, "add: [" + packageName + "]: " + extra + " (" + res + ")");
                }
            }
        }
    }

    private class ImageAdapter extends BaseAdapter implements ListAdapter, SpinnerAdapter {
        private LayoutInflater mLayoutInflater;

        ImageAdapter(Activity activity) {
            mLayoutInflater = activity.getLayoutInflater();
        }

        public int getCount() {
            return mThumbs.size();
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View view;

            if (convertView == null) {
                view = mLayoutInflater.inflate(R.layout.wallpaper_item, parent, false);
            } else {
                view = convertView;
            }

            ImageView image = (ImageView) view.findViewById(R.id.wallpaper_image);
            // ActionsCode( lizihao, new feature)
            if(mCustomWallpaperPath) {
                Bitmap imageBitmap = BitmapFactory.decodeFile("/system/etc/wallpaper/wallpaper_0" + position + "_small.png");
                image.setImageBitmap(imageBitmap);
            } else {
                int thumbRes = mThumbs.get(position);
                image.setImageResource(thumbRes);
            }
			
            Drawable thumbDrawable = image.getDrawable();
            if (thumbDrawable != null) {
                thumbDrawable.setDither(true);
            } else {
                Log.e(TAG, "Error decoding thumbnail resId=" +  " for wallpaper #"
                        + position);
            }

            return view;
        }
    }

    class WallpaperLoader extends AsyncTask<Integer, Void, Bitmap> {
        BitmapFactory.Options mOptions;
        WallpaperLoader() {
            mOptions = new BitmapFactory.Options();
            mOptions.inDither = false;
            mOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;
        }

        /**
        *
        *xxxxx
        *xxxxx
        *
        ************************************
        *      
        *ActionsCode(author:phchen, change_code)
        */
        @Override
        protected Bitmap doInBackground(Integer... params) {
            if (isCancelled()) return null;
            try {
                // add by lizhao@actions-semi.com for modify
                if(mCustomWallpaperPath) {
                    Bitmap bitmap = BitmapFactory.decodeFile("/system/etc/wallpaper/wallpaper_0" + params[0] + ".png");
                    return bitmap;
                } else {
                     //ActionsCode(phchen, BUGFIX: BUG00259001 )
                    // to make sure this fragmant has attched to the activity every single time
                    if(isAdded()) {
                        return BitmapFactory.decodeResource(getResources(),
                            mImages.get(params[0]), mOptions);
                   }else {
                       return null;
                   }
                    
                    //final Drawable d = getResources().getDrawable(mImages.get(params[0]));
                    //if (d instanceof BitmapDrawable) {
                        //return ((BitmapDrawable)d).getBitmap();
                    //}
                }
                //return null;
            } catch (OutOfMemoryError e) {
                Log.w(TAG, String.format(
                        "Out of memory trying to load wallpaper res=%08x", params[0]),
                        e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap b) {
            if (b == null) return;

            if (!isCancelled() && !mOptions.mCancel) {
                // Help the GC
                if (mBitmap != null) {
                    mBitmap.recycle();
                }

                View v = getView();
                if (v != null) {
                    mBitmap = b;
                    mWallpaperDrawable.setBitmap(b);
                    v.postInvalidate();
                } else {
                    mBitmap = null;
                    mWallpaperDrawable.setBitmap(null);
                }
                mLoader = null;
            } else {
               b.recycle();
            }
        }

        void cancel() {
            mOptions.requestCancelDecode();
            super.cancel(true);
        }
    }

    /**
     * Custom drawable that centers the bitmap fed to it.
     */
    static class WallpaperDrawable extends Drawable {

        Bitmap mBitmap;
        int mIntrinsicWidth;
        int mIntrinsicHeight;
        Matrix mMatrix;

        /* package */void setBitmap(Bitmap bitmap) {
            mBitmap = bitmap;
            if (mBitmap == null)
                return;
            mIntrinsicWidth = mBitmap.getWidth();
            mIntrinsicHeight = mBitmap.getHeight();
            mMatrix = null;
        }

        @Override
        public void draw(Canvas canvas) {
            if (mBitmap == null) return;
 
            if (mMatrix == null) {
                final int vwidth = canvas.getWidth();
                final int vheight = canvas.getHeight();
                final int dwidth = mIntrinsicWidth;
                final int dheight = mIntrinsicHeight;

                float scale = 1.0f;

                if (dwidth < vwidth || dheight < vheight) {
                    scale = Math.max((float) vwidth / (float) dwidth,
                            (float) vheight / (float) dheight);
                }

                float dx = (vwidth - dwidth * scale) * 0.5f + 0.5f;
                float dy = (vheight - dheight * scale) * 0.5f + 0.5f;

                mMatrix = new Matrix();
                mMatrix.setScale(scale, scale);
                mMatrix.postTranslate((int) dx, (int) dy);
            }

            canvas.drawBitmap(mBitmap, mMatrix, null);
        }

        @Override
        public int getOpacity() {
            return android.graphics.PixelFormat.OPAQUE;
        }

        @Override
        public void setAlpha(int alpha) {
            // Ignore
        }

        @Override
        public void setColorFilter(ColorFilter cf) {
            // Ignore
        }
    }
}
