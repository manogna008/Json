package com.example.user.json;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;


import com.example.user.json.modles.MoviesModle;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    ListView lvmovies;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lvmovies = (ListView)findViewById(R.id.lvmovies);
        // Create default options which will be used for every
        //  displayImage(...) call if no options will be passed to this method
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()

        .cacheInMemory(true)
                .cacheOnDisk(true)

        .build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())

        .defaultDisplayImageOptions(defaultOptions)

        .build();
        ImageLoader.getInstance().init(config); // Do it on Application start


    }

    public class Sync extends AsyncTask<String, String,  List<MoviesModle>> {
        StringBuffer buffer;

        @Override
        protected  List<MoviesModle> doInBackground(String... params) {
            HttpURLConnection connection = null;
            BufferedReader bufferedReader = null;

            try {
                URL url;
                url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                InputStream stream = connection.getInputStream();

                bufferedReader = new BufferedReader(new InputStreamReader(stream));
                buffer = new StringBuffer();

                String line = "";
                while ((line = bufferedReader.readLine()) != null) {
                    buffer.append(line);

                }
                 String finaljson = buffer.toString();

                JSONObject parentObject = new JSONObject(finaljson);
                JSONArray parentArray = parentObject.getJSONArray("movies");
                List<MoviesModle> moviesModleList = new ArrayList<>();

                for (int i=0; i<parentArray.length(); i++) {
                    JSONObject finalobject = parentArray.getJSONObject(i);
                    MoviesModle moviesModle = new MoviesModle();
                    moviesModle.setMovie(finalobject.getString("movie"));
                    moviesModle.setYear(finalobject.getInt("year"));
                    moviesModle.setRating((float) finalobject.getDouble("rating"));
                    moviesModle.setDuration(finalobject.getString("duration"));
                    moviesModle.setDirector(finalobject.getString("director"));
                    moviesModle.setTagline(finalobject.getString("tagline"));
                    moviesModle.setImage(finalobject.getString("image"));
                    moviesModle.setStory(finalobject.getString("story"));
                    List<MoviesModle.cast> castList= new ArrayList<>();

                    for (int j=0; j<finalobject.getJSONArray("cast").length(); j++){
                        MoviesModle.cast cas= new MoviesModle.cast();
                        cas.setName( finalobject.getJSONArray("cast").getJSONObject(j).getString("name"));
                        castList.add(cas);
                    }
                    moviesModle.setCastList(castList);
                    moviesModleList.add(moviesModle);
                }


                return moviesModleList;

            }
            catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (bufferedReader != null) {
                        bufferedReader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            return null;


        }


        @Override
        protected void onPostExecute( List<MoviesModle> result) {
            super.onPostExecute(result);
            MovieAdapter adapter = new MovieAdapter(getApplicationContext(),R.layout.row,result);
            lvmovies.setAdapter(adapter);

        }


    }

    public class MovieAdapter extends ArrayAdapter {
         private List<MoviesModle> moviesModleList;
        private int resource;
        private LayoutInflater inflater;
        public MovieAdapter(Context context, int resource, List<MoviesModle> objects) {
            super(context, resource, objects);
            moviesModleList = objects;
            this.resource=resource;
            inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
          if (convertView==null)
          {
              convertView =inflater.inflate(resource,null);
          }
            ImageView imageView;
            TextView movie;
            TextView tagline;
            TextView year;
            TextView duration;
            TextView director;
            RatingBar rating;
            TextView cast;
            TextView story;

            imageView =(ImageView)convertView.findViewById(R.id.ivicon);
            movie = (TextView)convertView.findViewById(R.id.tvmovies);
            tagline = (TextView)convertView.findViewById(R.id.tvtagline);
            year = (TextView)convertView.findViewById(R.id.tvyear) ;
            duration = (TextView)convertView.findViewById(R.id.tvduration);
            director = (TextView)convertView.findViewById(R.id.tvdirector);
            rating = (RatingBar)convertView. findViewById(R.id.ratebar);
            cast = (TextView)convertView.findViewById(R.id.tvcast);
            story = (TextView)convertView.findViewById(R.id.tvstory);
            ImageLoader.getInstance().displayImage(moviesModleList.get(position).getImage(), imageView);

            movie.setText(moviesModleList.get(position).getMovie());
            tagline.setText(moviesModleList.get(position).getTagline());
            year.setText("year:" + moviesModleList.get(position).getYear());
            duration.setText(moviesModleList.get(position).getDuration());
            director.setText(moviesModleList.get(position).getDirector());
            rating.setRating(moviesModleList.get(position).getRating()/2);

            StringBuffer stringBuffer = new StringBuffer();
            for (MoviesModle.cast cast1 : moviesModleList.get(position).getCastList()){
                stringBuffer.append(cast1.getName() + ",");
            }
            cast.setText(stringBuffer);
            story.setText(moviesModleList.get(position).getStory());




            return convertView;
        }
    }


    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }
    public boolean onOptionsItemSelected(MenuItem item) {

        int id=item.getItemId();
        if(id==R.id.action_refresh)
        {
            new Sync().execute("http://jsonparsing.parseapp.com/jsonData/moviesData.txt");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}




