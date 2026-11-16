/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package application.services;


public class SupabaseConfig {
    public static final String SUPABASE_URL =
            System.getenv("SUPABASE_URL");

    public static final String SUPABASE_KEY =
            System.getenv("SUPABASE_KEY");

    private SupabaseConfig() {
        // Prevent instantiation.
    }
}
