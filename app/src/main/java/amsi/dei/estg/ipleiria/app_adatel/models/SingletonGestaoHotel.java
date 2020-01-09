package amsi.dei.estg.ipleiria.app_adatel.models;

import android.content.Context;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import amsi.dei.estg.ipleiria.app_adatel.listeners.ProfilesListener;
import amsi.dei.estg.ipleiria.app_adatel.listeners.ReservasListener;
import amsi.dei.estg.ipleiria.app_adatel.listeners.UsersListener;
import amsi.dei.estg.ipleiria.app_adatel.utils.ReservaJsonParser;
import amsi.dei.estg.ipleiria.app_adatel.utils.UserJsonParser;

public class SingletonGestaoHotel implements ReservasListener, UsersListener, ProfilesListener {

    private  static RequestQueue volleyQueue = null;

    //private String token = "AMSI-TOKEN";
    private String mUrlAPIUSERS = "http://10.200.18.207:8081/api/users";
    private String mUrlAPIPROFILES = "http://10.200.18.207:8081/api/profiles";
    private String mUrlAPIRESERVAS = "http://10.200.18.207:8081/api/reservas";

    ///Adicionei
    private ArrayList<User> users;
    private ArrayList<Reserva> reservas;
    private ArrayList<Profile> profiles;

    private static SingletonGestaoHotel INSTANCE = null;
    private HotelBDHelper hotelBDHelper = null;

    private UsersListener userListener;
    private ReservasListener reservasListener;


    public static synchronized SingletonGestaoHotel getInstance(Context context) {
        if(INSTANCE == null){
            INSTANCE = new SingletonGestaoHotel(context);
            volleyQueue = Volley.newRequestQueue(context);
        }
        return INSTANCE;
    }

    private SingletonGestaoHotel(Context context) {
        ///Adicionei
        users = new ArrayList<>();
        profiles = new ArrayList<>();
        reservas = new ArrayList<>();

        hotelBDHelper = new HotelBDHelper(context);
    }


    // <--------------  Métodos para garantir que os dados da BD estão atualizados com os dados vindos da API -------------->


    // <----------------------------------- USERS ----------------------------------->

    public User getUserBD(int id){
        for (User u: users){
            if(u.getId() == id){
                return u;
            }
        }
        return null;
    }

    ///Adicionei
    public ArrayList<User> getUsersBD(){
        return users = hotelBDHelper.getAllUsersBD();
    }

    public void adicionarUserBD(User user){
        hotelBDHelper.adicionarUserBD(user);
    }

    public void adicionarUsersBD(ArrayList<User> users){
        hotelBDHelper.removerAllUsers();
    }

    public void removerUserBD(int id){
        User auxUser = getUserBD(id);

        if(auxUser != null){
            if(hotelBDHelper.removerUserBD(auxUser.getId())){
                users.remove(auxUser);
                System.out.println("--> User removido");
            }
        }
    }

    public void guardarUserBD(User user){
        if(!users.contains(user)){
            return;
        }

        User auxUser = getUserBD(user.getId());
        auxUser.setUsername(user.getUsername());
        auxUser.setEmail(user.getEmail());
        auxUser.setPassword(user.getPassword());

        if(hotelBDHelper.guardarUserBD(auxUser)){
            System.out.println("--> USer Guardado na BD");
        }
    }


    // <----------------------------------- PROFILE ----------------------------------->

    public Profile getProfileBD(int id_user){
        for (Profile p: profiles){
            if(p.getId_user() == id_user){
                return p;
            }
        }
        return null;
    }

    public ArrayList<Profile> getProfilesBD(){
        profiles = hotelBDHelper.getAllProfilesBD();
        return profiles;
    }

    public void adicionarProfileBD(Profile profile){
        hotelBDHelper.adicionarProfileBD(profile);
    }

    public void adicionarProfilesBD(ArrayList<Profile> profiles){
        hotelBDHelper.removerALLProfilesDB();
    }


    public void removerProfileBD(int id_user){
        Profile auxProfile = getProfileBD(id_user);

        if(auxProfile != null){
            if(hotelBDHelper.removerProfileBD(auxProfile.getId_user())){
                profiles.remove(auxProfile);
                System.out.println("--> Profile removido");
            }
        }
    }

    public void guardarProfileBD(Profile profile){
        if(!profiles.contains(profile)){
            return;
        }

        Profile auxProfile = getProfileBD(profile.getId_user());
        auxProfile.setNome(profile.getNome());
        auxProfile.setNif(profile.getNif());
        auxProfile.setTelefone(profile.getTelefone());
        auxProfile.setIs_admin(profile.getIs_admin());
        auxProfile.setIs_cliente(profile.getIs_cliente());
        auxProfile.setIs_funcionario(profile.getIs_funcionario());

        if(hotelBDHelper.guardarProfileBD(auxProfile)){
            System.out.println("--> Profile Guardado");
        }
    }


    // <----------------------------------- RESERVAS ----------------------------------->

    public ArrayList<Reserva> getReservasBD(){
        return reservas;
    }

    public Reserva getReservaBD(long idReserva){
        for(Reserva r: reservas){
            if(r.getId() == idReserva){
                return r;
            }
        }
        return null;
    }

    public void adicionarReservaBD(Reserva reserva){
        reservas.add(reserva);
    }

    public void adicionarReservasBD(ArrayList<Reserva> reservas){
        hotelBDHelper.removerALLReservasDB();
    }

    public void removerReservaBD(int idReserva){
        Reserva auxReserva = getReservaBD(idReserva);
        reservas.remove(auxReserva);
    }

    public void guardarReservaBD(Reserva reserva){
        if(!reservas.contains(reserva)){
            return;
        }
        Reserva auxReserva = getReservaBD(reserva.getId());
        auxReserva.setDtEntrada(reserva.getDtEntrada());
        auxReserva.setDtSaida(reserva.getDtSaida());
        auxReserva.setNumPessoas(reserva.getNumPessoas());
        //auxReserva.setNumQuartos(reserva.getNumQuartos());
    }




    // <----------------------------- Métodos para atualizarem a API ----------------------------->

    // <----------------------------------- USERS ----------------------------------->

    public  void getAllUsersAPI(final Context context, boolean isConected){
        Toast.makeText(context, "Is Connected", Toast.LENGTH_SHORT).show();

        if(!isConected){
            users = hotelBDHelper.getAllUsersBD();

            if(userListener != null){
               userListener.onRefreshListaUser(users);
            }
            else {
                JsonArrayRequest req = new JsonArrayRequest(Request.Method.GET, mUrlAPIUSERS, null, new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        //recebe todos os users como um objeto
                        users = UserJsonParser.parserJsonUsers(response, context);
                        System.out.println("--> Response Users: " + users);

                        adicionarUsersBD(users);

                        if(userListener != null){
                            userListener.onRefreshListaUser(users);
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        System.out.println("--> Erro: GetAllUsersApi: " + error.getMessage());
                    }
                });
                volleyQueue.add(req);
            }
        }
    }


    // <----------------------------------- PROFILES ----------------------------------->



    // <----------------------------------- RESERVAS ----------------------------------->

    // Vai buscar as reservas todas à API
    public void getAllReservasAPI(final Context context, boolean isConnected){

        Toast.makeText(context, "ISCONNECTED: " + isConnected, Toast.LENGTH_SHORT).show();
        if(!isConnected){
            //Toast.makeText(context, "NotConnected", Toast.LENGTH_SHORT).show();
            reservas = hotelBDHelper.getAllReservasBD();

            if(reservasListener != null){
                reservasListener.onRefreshListaReservas(reservas);
            }
        } else {
            //Toast.makeText(context, "Connected", Toast.LENGTH_SHORT).show();
            JsonArrayRequest req = new JsonArrayRequest(Request.Method.GET, mUrlAPIRESERVAS, null, new Response.Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray response) {

                    reservas = ReservaJsonParser.parserJsonLReservas(response, context);
                    System.out.println("--> RESPOSTA: " + reservas);
                    adicionarReservasBD(reservas);

                    if(reservasListener != null){
                        reservasListener.onRefreshListaReservas(reservas);
                    }

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    System.out.println("--> ERRO: getAllReservasAPI: " + error.getMessage());
                }
            });

            volleyQueue.add(req);
        }
    }

    // Adicionar 1 só livro à API
    public void adicionarReservaAPI(final Reserva reserva, final Context context){

        StringRequest req = new StringRequest(Request.Method.POST, mUrlAPIRESERVAS, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                System.out.println("--> RESPOSTA ADD POST: " + response);

                if(reservasListener != null){
                    reservasListener.onUpdateListaReservasBD(ReservaJsonParser.parserJsonReservas(response, context), 1);
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("--> ERRO: adicionarReservasAPI: " + error.getMessage());
            }
        }){
            protected Map<String, String> getParams(){

                Map<String, String> params = new HashMap<>();
                params.put("num_pessoas", reserva.getNumPessoas() + "");
                //params.put("num_quartos", reserva.getNumQuartos() + "");        // Os Nomes
                params.put("quarto_solteiro", reserva.getQuartoSol() + "");     // têm de
                params.put("quarto_duplo", reserva.getQuartoD() + "");          // corresponder
                params.put("quarto_familia", reserva.getQuartoF() + "");        // aos da
                params.put("quarto_casal", reserva.getQuartoC() + "");          // API
                params.put("data_entrada", reserva.getDtEntrada());
                params.put("data_saida", reserva.getDtSaida());

                return params;
            }
        };
        volleyQueue.add(req);
    }

    // Remove a reserva da API
    public void removerReservaAPI(final Reserva reserva){

        final StringRequest req = new StringRequest(Request.Method.DELETE, mUrlAPIRESERVAS + '/' + reserva.getId(), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                System.out.println("--> RESPOSTA REMOVER: " + response);

                if(reservasListener != null){
                    reservasListener.onUpdateListaReservasBD(reserva,3);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                System.out.println("--> ERRO: removerReservaAPI: " + error.getMessage());
            }
        });
        volleyQueue.add(req);
    }

    // Atualiza a reserva na API
    public void editarReservaAPI(final Reserva reserva, final Context context){

        StringRequest req = new StringRequest(Request.Method.PUT, mUrlAPIRESERVAS + '/' + reserva.getId(), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                System.out.println("--> editarReservaAPI: " + response);

                if(reservasListener != null){
                    reservasListener.onUpdateListaReservasBD(ReservaJsonParser.parserJsonReservas(response, context), 2);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                System.out.println("--> ERRO: editarReservaAPI: " + error.getMessage());
            }
        }){
            protected Map<String, String> getParams(){

                Map<String, String> params = new HashMap<>();
                params.put("num_pessoas", reserva.getNumPessoas() + "");
               // params.put("num_quartos", reserva.getNumQuartos() + "");         // Os Nomes
                params.put("quarto_solteiro", reserva.getQuartoSol() + "");      // têm de
                params.put("quarto_duplo", reserva.getQuartoD() + "");           // corresponder
                params.put("quarto_familia", reserva.getQuartoF() + "");         // aos da
                params.put("quarto_casal", reserva.getQuartoC() + "");           // API
                params.put("data_entras", reserva.getDtEntrada());
                params.put("data_saida", reserva.getDtSaida());

                return params;
            }
        };
        volleyQueue.add(req);
    }


    public void setReservasListener(ReservasListener reservasListener){

        this.reservasListener = reservasListener;
    }




    // <------------------------------------------- Métodos OnRefresh e OnUpdate ------------------------------------------->

    // <--------------------------------------- USERS --------------------------------------->

    @Override
    public void onRefreshListaUser(ArrayList<User> listaLivros) {

    }

    @Override
    public void onUpdateListaUserBD(User user, int operacao) {

    }


    // <--------------------------------------- PROFILES --------------------------------------->

    @Override
    public void onRefreshListaProfiles(ArrayList<Profile> listaProfiles) {

    }

    @Override
    public void onUpdateListaProfilesBD(Profile livro, int operacao) {

    }


    // <--------------------------------------- RESERVAS --------------------------------------->

    @Override
    public void onRefreshListaReservas(ArrayList<Reserva> listaReservas) {

    }

    @Override
    public void onUpdateListaReservasBD(Reserva reserva, int operacao) {

        System.out.println("--> Entrou update lista reservasBD");

        switch (operacao){
            case 1: adicionarReservaBD(reserva);
                break;
            case 2: guardarReservaBD(reserva);
                break;
            case 3: removerReservaBD(reserva.getId());
                break;

        }
    }



    // <----------------------------------------------------------------------------------------------------->

    private void gerarFakeData(){
        /*
            reservas.add(new Reserva(1, 2, 2, 0, 1,0, 1, "9/12/2019", "15/12/2019"));
            reservas.add(new Reserva(2, 1, 1, 1, 0,0, 0, "23/12/2019", "26/12/2019"));
            reservas.add(new Reserva(3, 6, 3, 0, 0,2, 1, "8/01/2020", "14/01/2020"));
            reservas.add(new Reserva(4, 3, 2, 1, 1,0, 0, "20/01/2020", "24/01/2020"));
         */
    }
}
