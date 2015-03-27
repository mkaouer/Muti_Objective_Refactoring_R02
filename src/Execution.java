
import java.util.ArrayList;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author MWM
 */
public class Execution 
{
    //--- User parameters ---
    // these values represents the chosen reference point and sigma value
    
    static double aspiration_values[] = {0.5,0.5};
    static ReferencePoint ref = new ReferencePoint(aspiration_values) ;
    
    static Sigma s = new Sigma(1); // r-NSGA 2
    
    static int generations = 10 ;
    
    static int population_size = 20 ;
    
    Execution()
    {
        
    }
    
    public static void main(String[] args) 
    {
        Population p = new Population(population_size,s,ref,generations); 
       
        ArrayList<Solution> parents = new ArrayList<Solution>();
        
        p.create_poplulation();
        
        for(int i=0; i<generations; i++)
        {
            p.update_sigma_value(i, generations);
            parents = p.random_selection();
            p.generate_next_popluation(parents,i);
            //p.print_popluation_metrics(i);
        }
        
        
    }
}


