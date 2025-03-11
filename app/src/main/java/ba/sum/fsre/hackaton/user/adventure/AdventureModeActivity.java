package ba.sum.fsre.hackaton.user.adventure;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.LocationBias;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import android.view.View;
import android.widget.AdapterView;

import ba.sum.fsre.hackaton.R;

public class AdventureModeActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "AdventureModeActivity";
    private ListView placesListView;
    private GoogleMap googleMap;
    private static final LatLng ZAGREB_LOCATION = new LatLng(45.8150, 15.9819);
    private static final LatLng PARIS_LOCATION = new LatLng(48.8566, 2.3522);
    private LatLng selectedLocation;
    private String selectedCity;
    private ArrayAdapter<String> placesAdapter;
    private List<String> placesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adventure_mode);

        // Get selected values from intent
        String nativeLanguage = getIntent().getStringExtra("nativeLanguage");
        String learningLanguage = getIntent().getStringExtra("learningLanguage");
        String city = getIntent().getStringExtra("city");

        // Initialize the Places API
        Places.initialize(getApplicationContext(), getString(R.string.google_maps_key));

        // Initialize ListView
        placesListView = findViewById(R.id.placesListView);
        placesList = new ArrayList<>();
        placesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, placesList);
        placesListView.setAdapter(placesAdapter);

        // Initialize SupportMapFragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Set selected location based on city
        if (city.equals("Zagreb")) {
            selectedLocation = ZAGREB_LOCATION;
            selectedCity = "Zagreb";
        } else if (city.equals("Paris")) {
            selectedLocation = PARIS_LOCATION;
            selectedCity = "Paris";
        }

        // Set item click listener for ListView
        placesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String placeName = placesList.get(position);
                String learningLanguage = getIntent().getStringExtra("learningLanguage");
                String nativeLanguage = getIntent().getStringExtra("nativeLanguage");
                Intent intent = new Intent(AdventureModeActivity.this, ChallengeDetailActivity.class);
                intent.putExtra("challengeTitle", placeName);
                intent.putExtra("learningLanguage", learningLanguage);
                intent.putExtra("nativeLanguage", nativeLanguage);
                intent.putExtra("challengeDescription", "Naruči kavu na " + learningLanguage + " jeziku u " + placeName);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        updateMapAndPlaces(selectedLocation, selectedCity);
    }

    private void updateMapAndPlaces(LatLng location, String city) {
        if (googleMap != null) {
            googleMap.clear();
            googleMap.addMarker(new MarkerOptions().position(location).title("Marker in " + city));
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15)); // Adjust the zoom level here
        }
        new FetchPlacesTask().execute(location);
    }

    private class FetchPlacesTask extends AsyncTask<LatLng, Void, List<MarkerOptions>> {
        @Override
        protected List<MarkerOptions> doInBackground(LatLng... locations) {
            List<MarkerOptions> markerOptionsList = new ArrayList<>();
            try {
                LatLng location = locations[0];
                LocationBias locationBias = RectangularBounds.newInstance(
                        new LatLng(location.latitude - 0.1, location.longitude - 0.1),
                        new LatLng(location.latitude + 0.1, location.longitude + 0.1)
                );
                PlacesClient placesClient = Places.createClient(AdventureModeActivity.this);

                // Queries to fetch more places
                List<String> queries = Arrays.asList("restaurant", "cafe", "museum", "park", "hotel");

                for (String query : queries) {
                    FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                            .setLocationBias(locationBias)
                            .setQuery(query)
                            .setTypesFilter(Arrays.asList("establishment", "geocode"))
                            .build();
                    Task<FindAutocompletePredictionsResponse> placeResponseTask = placesClient.findAutocompletePredictions(request);
                    FindAutocompletePredictionsResponse response = Tasks.await(placeResponseTask);
                    for (AutocompletePrediction prediction : response.getAutocompletePredictions()) {
                        String placeId = prediction.getPlaceId();
                        FetchPlaceRequest fetchPlaceRequest = FetchPlaceRequest.builder(placeId, Arrays.asList(Place.Field.LAT_LNG)).build();
                        Task<FetchPlaceResponse> placeTask = placesClient.fetchPlace(fetchPlaceRequest);
                        FetchPlaceResponse fetchPlaceResponse = Tasks.await(placeTask);
                        Place place = fetchPlaceResponse.getPlace();
                        LatLng placeLocation = place.getLatLng();
                        if (placeLocation != null) {
                            markerOptionsList.add(new MarkerOptions().position(placeLocation).title(prediction.getPrimaryText(null).toString()));
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return markerOptionsList;
        }

        @Override
        protected void onPostExecute(List<MarkerOptions> markerOptionsList) {
            placesList.clear();
            for (MarkerOptions markerOptions : markerOptionsList) {
                googleMap.addMarker(markerOptions);
                placesList.add(markerOptions.getTitle());
            }
            placesAdapter.notifyDataSetChanged();
        }
    }
}