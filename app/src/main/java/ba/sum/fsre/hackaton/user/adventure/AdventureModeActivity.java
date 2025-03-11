// AdventureModeActivity.java
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import android.view.View;

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
                String category = getCategoryForPlace(placeName);

                fetchRandomChallenge(category, placeName, learningLanguage, nativeLanguage);
            }
        });
    }

    private void fetchRandomChallenge(String category, String placeName, String learningLanguage, String nativeLanguage) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // Convert category to lowercase to match Firestore document structure
        final String finalCategory = category.toLowerCase();
        db.collection("challenges").document(finalCategory).collection("challenges")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            Random random = new Random();
                            int randomIndex = random.nextInt(querySnapshot.size());
                            DocumentSnapshot document = querySnapshot.getDocuments().get(randomIndex);
                            String challengeDescription = document.getString("challenge");
                            String challengeTitle = document.getString("title");

                            Log.d(TAG, "Challenge Title: " + challengeTitle);
                            Log.d(TAG, "Challenge Description: " + challengeDescription);

                            Intent intent = new Intent(AdventureModeActivity.this, ChallengeDetailActivity.class);
                            intent.putExtra("challengeTitle", challengeTitle);
                            intent.putExtra("learningLanguage", learningLanguage);
                            intent.putExtra("nativeLanguage", nativeLanguage);
                            intent.putExtra("challengeDescription", challengeDescription);
                            intent.putExtra("category", finalCategory);
                            startActivity(intent);
                        } else {
                            Log.e(TAG, "No challenges found for category: " + finalCategory);
                        }
                    } else {
                        Log.e(TAG, "Error getting documents: ", task.getException());
                    }
                });
    }

    private String getCategoryForPlace(String placeName) {
        placeName = placeName.toLowerCase();
        if (placeName.contains("restaurant")) {
            return "Restaurant";
        } else if (placeName.contains("museum")) {
            return "Museum";
        } else if (placeName.contains("cafe") || placeName.contains("caffe") || placeName.contains("bar") || placeName.contains("bistro") ) {
            return "Caffe bar";
        } else if (placeName.contains("landmark")) {
            return "Landmark";
        }  else if (placeName.contains("pub")) {
            return "Pub";
        } else if (placeName.contains("statue")) {
            return "Statue";
        } else if (placeName.contains("monument")) {
            return "Monument";
        } else if (placeName.contains("church")) {
            return "Church";
        } else if (placeName.contains("cathedral")) {
            return "Cathedral";
        } else if (placeName.contains("mosque")) {
            return "Mosque";
        } else if (placeName.contains("synagogue")) {
            return "Synagogue";
        } else if (placeName.contains("park")) {
            return "Park";
        } else if (placeName.contains("square")) {
            return "Square";
        } else if (placeName.contains("garden")) {
            return "Garden";
        } else if (placeName.contains("theater")) {
            return "Theater";
        } else if (placeName.contains("gallery")) {
            return "Gallery";
        } else if (placeName.contains("exhibition")) {
            return "Exhibition";
        } else if (placeName.contains("library")) {
            return "Library";
        } else if (placeName.contains("fountain")) {
            return "Fountain";
        } else if (placeName.contains("bridge")) {
            return "Bridge";
        } else if (placeName.contains("tourist attraction")) {
            return "Tourist attraction";
        } else if (placeName.contains("cultural center")) {
            return "Cultural center";
        } else {
            return "Other";
        }
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
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15));
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

                List<String> queries = Arrays.asList("restaurant", "cafe", "museum", "caffe", "bar", "bistro", "pub",
                        "landmark", "statue", "monument", "church",
                        "cathedral", "mosque", "synagogue",
                        "park", "square", "garden", "theater", "gallery", "exhibition",
                        "library", "fountain", "bridge", "tourist attraction", "cultural center");

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