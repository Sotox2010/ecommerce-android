package com.smartsecurity.android.piudonnacouture.sync;

import com.google.gson.JsonObject;
import com.smartsecurity.android.piudonnacouture.model.Address;
import com.smartsecurity.android.piudonnacouture.model.AuthTokenResponse;
import com.smartsecurity.android.piudonnacouture.model.BlogEntry;
import com.smartsecurity.android.piudonnacouture.model.Brand;
import com.smartsecurity.android.piudonnacouture.model.CartItem;
import com.smartsecurity.android.piudonnacouture.model.Category;
import com.smartsecurity.android.piudonnacouture.model.Order;
import com.smartsecurity.android.piudonnacouture.model.Subcategory;
import com.smartsecurity.android.piudonnacouture.model.UserInfo;

import java.util.List;

import retrofit.client.Response;
import retrofit.http.DELETE;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Query;

public interface WebService {

    String PATH_AUTH_TOKEN = "/oauth/v2/token";
    String PATH_CREATE_ACCOUNT = "/web-service/signUp";
    String PATH_USER_INFO = "/web-service/user";

    String PATH_CATEGORIES = "/categories";
    String PATH_SUBCATEGORIES = "/subcategories";
    String PATH_BRANDS = "/brands";
    String PATH_PRODUCT = "/web-service/product";
    String PATH_PRODUCT_STOCK = "/web-service/product/stock";
    String PATH_PRODUCTS = "/products";
    String PATH_STORE = "/web-service/store";
    String PATH_BLOG_FEED = "/web-service/news";
    String PATH_SHOPPING_CART = "/web-service/shoppingCart";
    String PATH_WISHLIST = "/web-service/wishlist";
    String PATH_ADDRESS_BOOK = "/web-service/address-book";
    String PATH_ORDERS = "/web-service/orders";
    String PATH_ORDER_DETAILS = "/web-service/order-details";
    String PATH_PLACE_ORDER = "/web-service/place-order";

    String HEADER_IF_MODIFIED_SINCE = "If-Modified-Since";
    String HEADER_AUTHORIZATION = "Authorization";

    String FIELD_EMAIL = "email";
    String FIELD_USERNAME = "username";
    String FIELD_PASSWORD = "password";
    String FIELD_REFRESH_TOKEN = "refresh_token";
    String FIELD_FIRST_NAME = "firstName";
    String FIELD_LAST_NAME = "lastName";
    String FIELD_COMPANY = "companyName";
    String FIELD_CLIENT_ID = "client_id";
    String FIELD_CLIENT_SECRET = "client_secret";
    String FIELD_GRANT_TYPE = "grant_type";

    @FormUrlEncoded
    @POST(PATH_AUTH_TOKEN)
    AuthTokenResponse getAuthToken(
            @Field(FIELD_USERNAME) String email,
            @Field(FIELD_PASSWORD) String password,
            @Field(FIELD_CLIENT_ID) String clientId,
            @Field(FIELD_CLIENT_SECRET) String clientSecret,
            @Field(FIELD_GRANT_TYPE) String grantType);

    @FormUrlEncoded
    @POST(PATH_AUTH_TOKEN)
    AuthTokenResponse refreshAccessToken(
            @Field(FIELD_REFRESH_TOKEN) String refreshToken,
            @Field(FIELD_CLIENT_ID) String clientId,
            @Field(FIELD_CLIENT_SECRET) String clientSecret,
            @Field(FIELD_GRANT_TYPE) String grantType);

    @GET(PATH_USER_INFO)
    UserInfo getUserInfo(
            @Header(HEADER_AUTHORIZATION) String authToken,
            @Query(FIELD_EMAIL) String email);

    @FormUrlEncoded
    @POST(PATH_CREATE_ACCOUNT)
    Response createAccount(
            @Field(FIELD_FIRST_NAME) String fistName,
            @Field(FIELD_LAST_NAME) String lastName,
            @Field(FIELD_EMAIL) String email,
            @Field(FIELD_PASSWORD) String password,
            @Field(FIELD_COMPANY) String company
    );

    @GET(PATH_CATEGORIES)
    List<Category> getCategories(
            @Header(HEADER_IF_MODIFIED_SINCE) String lastModifiedDate
    );

    @GET(PATH_SUBCATEGORIES)
    List<Subcategory> getSubcategories(
            @Header(HEADER_IF_MODIFIED_SINCE) String lastModifiedDate
    );

    @GET(PATH_BRANDS)
    List<Brand> getBrands(
            @Header(HEADER_IF_MODIFIED_SINCE) String lastModifiedDate
    );

    @GET(PATH_PRODUCTS)
    List<String> getProducts();

    @GET(PATH_PRODUCT)
    JsonObject getProductDetails(@Query("productCode") String code);

    @GET(PATH_PRODUCT_STOCK)
    JsonObject getProductStock(
            @Query("productCode") String code,
            @Query("colorId") long colorId,
            @Query("sizeId") long sizeId);

    @GET(PATH_STORE)
    JsonObject getStoreData(
            @Header(HEADER_IF_MODIFIED_SINCE) String lastModifiedDate
    );

    @GET(PATH_BLOG_FEED)
    List<BlogEntry> getBlogFeed();

    @GET(PATH_SHOPPING_CART)
    List<CartItem> getShoppingCart(
            @Query(FIELD_EMAIL) String email,
            @Header(HEADER_AUTHORIZATION) String authToken
    );

    @FormUrlEncoded
    @PUT(PATH_SHOPPING_CART)
    JsonObject addToCart(
            @Header(HEADER_AUTHORIZATION) String authToken,
            @Field(FIELD_EMAIL) String email,
            @Field("productCode") String code,
            @Field("colorId") String colorId,
            @Field("sizeId") String sizeId,
            @Field("quantity") String quantity
    );

    @DELETE(PATH_SHOPPING_CART)
    Response removeFromCart(
            @Header(HEADER_AUTHORIZATION) String authToken,
            @Query(FIELD_EMAIL) String email,
            @Query("productCode") String productCode,
            @Query("cartId") String cartItemId
    );

    @FormUrlEncoded
    @POST(PATH_SHOPPING_CART)
    Response updateCartItem(
            @Header(HEADER_AUTHORIZATION) String authToken,
            @Field(FIELD_EMAIL) String email,
            @Field("cartId") String cartItemId,
            @Field("quantity") String newQuantity
    );

    @GET(PATH_WISHLIST)
    String getWishlist(
            @Header(HEADER_AUTHORIZATION) String authToken
    );

    @PUT(PATH_WISHLIST)
    void addToWishlist(
            @Header(HEADER_AUTHORIZATION) String authToken
    );

    @DELETE(PATH_WISHLIST)
    void removeFromWishlist(
            @Header(HEADER_AUTHORIZATION) String authToken
    );

    @GET(PATH_ADDRESS_BOOK)
    List<Address> getAddressBook(
            @Header(HEADER_AUTHORIZATION) String authToken,
            @Query(FIELD_EMAIL) String email);

    @POST(PATH_ADDRESS_BOOK)
    Response updateAddress(
            @Header(HEADER_AUTHORIZATION) String authToken,
            @Field(FIELD_EMAIL) String email,
            @Field("addressId") String addressId,
            @Field("fullName") String fullName,
            @Field("lineOne") String addressLine1,
            @Field("lineTwo") String addressLine2,
            @Field("city") String city,
            @Field("state") String state,
            @Field("zip") String zipCode,
            @Field("country") String country,
            @Field("phoneNumber") String phone);

    @FormUrlEncoded
    @PUT(PATH_ADDRESS_BOOK)
    JsonObject addNewAddress(
            @Header(HEADER_AUTHORIZATION) String authToken,
            @Field(FIELD_EMAIL) String email,
            @Field("fullName") String fullName,
            @Field("lineOne") String addressLine1,
            @Field("lineTwo") String addressLine2,
            @Field("city") String city,
            @Field("state") String state,
            @Field("zip") String zipCode,
            @Field("country") String country,
            @Field("phoneNumber") String phone);

    @DELETE(PATH_ADDRESS_BOOK)
    Response removeAddress(
            @Header(HEADER_AUTHORIZATION) String authToken,
            @Query(FIELD_EMAIL) String email,
            @Query("addressId") String addressId);

    @GET(PATH_ORDERS)
    List<Order> getOrders(
            @Header(HEADER_AUTHORIZATION) String authToken,
            @Query(FIELD_EMAIL) String email);

    @GET(PATH_ORDER_DETAILS)
    JsonObject getOrderDetails(
            @Header(HEADER_AUTHORIZATION) String authToken,
            @Query(FIELD_EMAIL) String email,
            @Query("orderId") String orderId);

    @FormUrlEncoded
    @POST(PATH_PLACE_ORDER)
    JsonObject placeOrder(
            @Header(HEADER_AUTHORIZATION) String authToken,
            @Field(FIELD_EMAIL) String email,
            @Field("addressId") String addressId,
            @Field("card_name") String nameOnCard,
            @Field("card_number") String cardNumber,
            @Field("card_cvc") String cvc,
            @Field("card_month") String expMonth,
            @Field("card_year") String expYear);
}
