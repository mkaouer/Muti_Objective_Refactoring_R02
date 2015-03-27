/**
 * Many-Objective Refactoring
 * @author MWM
 */

import java.io.*;
import java.text.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.*;
import java.lang.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class Solution 
{
    
    //list of candiate refactoring operations to apply
	public static String[] refactoring={"addGeneralisation","deleteGeneralisation",
		"addRelationShip","deleteRelationShip","moveAttribute","moveParameter",
		"moveMethod","extractClass","pullUpAttribute","pullUpMethod","pushDownAttribute",
		"pushDownMethod"};
    
    ArrayList<Integer> refectorings ;
    
    // just for printing or not output actions
    public static boolean print_executed_refactorings = false ;
    
    // number of objectives to be considered
    //static int objectives_number = 4;
    ArrayList<Double> objectives ;
    ArrayList<String> objectives_names ;
    
    int number_of_blobs ;
    
    
    static boolean coupling_metric = true;
    static boolean cohesion_metric= true;
    static boolean complexity_metric= false;
    static boolean stability_metric= false;
    static boolean deviance_metric= false;
    static boolean encapsulation_metric= false;
    static boolean interfacing_metric= false;
     
    // Objectives detailed
    double complexity;
    double stability ;
       
    // these values represents the existing refactoring operations
    static int min_refactorings_interval = 0 ;
    static int max_refactorings_interval = 11 ;
    
    // User defined values
    static int min_solution_size = 10 ;
    static int max_solution_size = 20 ;
    
   
    
    Refactorings f;
    int effort ;
    
    int rank ;
    double dominance ;
    double distance ;
    double crowding_distance;
    
    Solution()
    {
        this.refectorings = new ArrayList<Integer>();
        this.complexity = 0 ;
        this.stability = 0 ;
        this.effort = 0 ;
        this.f = new Refactorings();
        this.rank = 0 ;
        this.dominance = 0 ;
        this.distance = 0 ;
        this.crowding_distance = 0 ;
        this.objectives = new ArrayList<Double>() ;
        this.objectives_names = new ArrayList<String>() ;
    }
    
    Solution(Solution a)
    {
        this.refectorings = new ArrayList<Integer>(a.refectorings);
        this.complexity = a.complexity ;
        this.stability = a.stability ;
        this.effort = a.effort ;
        this.f = new Refactorings();
        this.rank = a.rank ;
        this.dominance = a.dominance ;
        this.distance = a.distance ; 
        this.crowding_distance = a.crowding_distance ;
        
        this.objectives = new ArrayList<Double>() ;
        
        for(int i=0;i<a.objectives.size();i++)
        {
            this.objectives.add(a.objectives.get(i)) ;
        }
        
        this.objectives_names = new ArrayList<String>() ;
        
        for(int i=0;i<a.objectives_names.size();i++)
        {
            this.objectives_names.add(a.objectives_names.get(i)) ;
        }
        
    }
    
    void create_solution()
    {
        // creation of a sequence of refactorings
        int sol = Random.random(min_solution_size, max_solution_size);
        
        for(int i=0;i<sol;i++)
        {
            int numRefactoring=Random.random(min_refactorings_interval, max_refactorings_interval);
            this.refectorings.add(numRefactoring);
        } 
    }
    
    void create_solution_express()
    {
        // creation of a sequence of refactorings
        int sol = Random.random(min_solution_size, max_solution_size);
        
        for(int i=0;i<sol;i++)
        {
            int numRefactoring=Random.random(min_refactorings_interval, max_refactorings_interval);
            this.refectorings.add(numRefactoring);
        }
        
        // Evaluating the solution by calculating its metrics
        this.evaluate_solution();
    }
    
    // calculate metrcis for a given solution
    void evaluate_solution()
    {
        this.objectives = new ArrayList<Double>() ;
        this.objectives_names = new ArrayList<String>() ;
        
        for(int i=0;i<this.refectorings.size();i++)
        {
            //System.out.println("------------------------------- Starting Refactoring: "+refactoring[this.refectorings.get(i)]+"-------------------");
            effort+= f.executeRefactoring(this.refectorings.get(i));
        }
        
        // setting coupling & cohesion & complexity
        //System.out.print("\n starting all metrics");
        this.all_metrics();
    }
    
    void coupling_cohesion_complexity()
    {
	String bloc=new String();
	int nbClass=Info.getNbrClass();
        
        int local_coupling = 0 ; // for each class
        int local_cohesion = 0 ; // for each class
        double coupling = 0 ; // for all classses
        double cohesion = 0 ;
        
        double complexity[] ;
        
        complexity = new double[nbClass];
		
	for(int i=0;i<nbClass;i++)
	{
            bloc=(String)ApplyRefactoring.blocs.elementAt(i); // return class i
            String className = Info.getClassName(bloc);
            while(bloc.contains("Relation("))
		{
                    int d=bloc.indexOf("Relation(");
			int f=bloc.indexOf(");",d);
			String relation = bloc.substring(d,f);
                        String source = relation.substring((relation.indexOf("(")+1),relation.indexOf(";"));
                        String target = relation.substring((relation.indexOf(",")+1),relation.lastIndexOf(","));
                        if (source.equals(target)) {cohesion ++ ;local_cohesion ++ ;}
                        else if (className.equals(source)){coupling++;local_coupling++;}
                        String s1=bloc.substring(0,d);
			String s2=bloc.substring(f,bloc.length());
			bloc=s1+s2;
		}
            if(Metric.NOM(className) == 0)
            {
                complexity[i] = 0 ;
            }
            else
            {
                complexity[i] = (double)(local_coupling+local_cohesion)/(Metric.NOM(className));
            }
            local_coupling = 0 ;
            local_cohesion = 0 ;
	}
	coupling = (double) coupling/nbClass ;
        cohesion = (double) cohesion/nbClass ;
        
        if (coupling_metric)
        {
            objectives.add(coupling);
            this.objectives_names.add("Coupling");
        }
        
        if (cohesion_metric)
        {
            // Cohesion to be maximized so the inverse will be considered to have all objectives minimized
            objectives.add((double)(cohesion));
            this.objectives_names.add("Cohesion");
        }
        
        if (complexity_metric)
        {
            objectives.add(average(complexity));
            this.objectives_names.add("Complxity");
        }
        
    }
    
    void all_metrics()
    {
	String bloc=new String();
        String bloc1=new String();
        
	int nbClass=Info.getNbrClass();
        
        int local_coupling = 0 ; // for each class
        int local_cohesion = 0 ; // for each class
        double coupling = 0 ; // for all classses
        double cohesion = 0 ;
        
        double complexity[] ;
        
        complexity = new double[nbClass];
        
        double call_in = 0 ;
        double call_out = 0 ;
        double nbInterface = 0;
        
        double [] stability ;
        int number [];
        double public_attributes [];
        double total_attributes [];
        double direct_access [];
        
        stability = new double [nbClass];
        number = new int [nbClass];
        public_attributes = new double [nbClass];
        total_attributes = new double [nbClass];
        direct_access = new double [nbClass];
		
        for(int i=0;i<nbClass;i++)
	{
            public_attributes[i]=0;
            total_attributes[i]=0;
            direct_access[i]=0;
            stability[i]=0;
        }
        
	for(int i=0;i<nbClass;i++)
	{
            bloc=(String)ApplyRefactoring.blocs.elementAt(i); // return class i
            bloc1 = new String(bloc);
            
            String className = Info.getClassName(bloc);
            
            number[i] = Metric.NOM(className);
            public_attributes[i] = Info.getNbrPublicAttributes(className);
            total_attributes[i] = Info.getNbrAttributes(className);
            if(Info.isInterface((String)ApplyRefactoring.blocs.elementAt(i))) nbInterface++;
            
            String match = "Relation("+className;
            while(bloc1.contains(match))
		{
                    //System.out.println("\n call out on class "+i);
                    call_out++;
                    bloc1 = bloc1.replace(match, " ");
		}
            
            while(bloc.contains("Relation("))
		{
                    int d=bloc.indexOf("Relation(");
			int f=bloc.indexOf(");",d);
			String relation = bloc.substring(d,f);
                        String source = relation.substring((relation.indexOf("(")+1),relation.indexOf(";"));
                        String target = relation.substring((relation.indexOf(",")+1),relation.lastIndexOf(","));
                        if (source.equals(target)) {cohesion ++ ;local_cohesion ++ ;}
                        else if (className.equals(source)){coupling++;local_coupling++;}
                        String s1=bloc.substring(0,d);
			String s2=bloc.substring(f,bloc.length());
			bloc=s1+s2;
		}
            if(Metric.NOM(className) == 0)
            {
                complexity[i] = 0 ;
            }
            else
            {
                complexity[i] = (double)(local_coupling+local_cohesion)/(Metric.NOM(className));
            }
            if (call_out + call_in !=0)
            {
                //stability[i] = (double) call_out / (call_out + call_in) ;
                stability[i] = (double) call_out;
            }
            else
            {
                stability[i] = 0 ;
                //System.out.println("\n stability 0 on class "+i);
            }
            if (total_attributes[i] !=0)
            {
                direct_access[i] = (double) public_attributes[i]/total_attributes[i];
            }
            else
            {
                direct_access[i] = 0;
                //System.out.println("\n direct_access 0 on class "+i);
            }
            
            call_in = 0 ;
            call_out = 0 ;
            
            local_coupling = 0 ;
            local_cohesion = 0 ;
            
	}
	coupling = (double) coupling/nbClass ;
        cohesion = (double) cohesion/nbClass ;
        
        if (coupling_metric)
        {
            objectives.add(coupling);
            this.objectives_names.add("Coupling");
        }
        
        if (cohesion_metric)
        {
            // Cohesion to be maximized so the inverse will be considered to have all objectives minimized
            objectives.add((double)(cohesion));
            this.objectives_names.add("Cohesion");
        }
        
        if (complexity_metric)
        {
            objectives.add(average(complexity));
            this.objectives_names.add("Complxity");
        }
        if (stability_metric)
        {
            objectives.add(average(stability));
            this.objectives_names.add("Sability");
        }
        
        if (deviance_metric)
        {
            objectives.add(standardDeviationCalculate(number));
            this.objectives_names.add("Standard Deviation");
        }
        
        if (encapsulation_metric)
        {
            objectives.add(average(direct_access));
            this.objectives_names.add("Encapsulation");
        }
        
        if (interfacing_metric)
        {
            // interface per classes ratio to be maximized so the inverse will be considered to have all objectives minimized
            objectives.add((double)((nbInterface/nbClass)));
            this.objectives_names.add("Interfacing");
        }
        
    }
    
    void stability_standardDeviation_encapsulation_interfacing()
    {
	double call_in = 0 ;
        double call_out = 0 ;
        double nbInterface = 0;
        
        double [] stability ;
        int number [];
        double public_attributes [];
        double total_attributes [];
        double direct_access [];
        
        
        String bloc=new String();
	int nbClass=Info.getNbrClass();
        
        stability = new double [nbClass];
        number = new int [nbClass];
        public_attributes = new double [nbClass];
        total_attributes = new double [nbClass];
        direct_access = new double [nbClass];
        
        for(int i=0;i<nbClass;i++)
	{
            public_attributes[i]=0;
            total_attributes[i]=0;
            direct_access[i]=0;
            stability[i]=0;
        }
        
	for(int i=0;i<nbClass;i++)
	{
            //System.out.println("\n starting with class i : "+i);
            bloc=(String)ApplyRefactoring.blocs.elementAt(i); // return class i
            String className = Info.getClassName(bloc);
            number[i] = Metric.NOM(className);
            public_attributes[i] = Info.getNbrPublicAttributes(className);
            total_attributes[i] = Info.getNbrAttributes(className);
            if(Info.isInterface((String)ApplyRefactoring.blocs.elementAt(i))) nbInterface++;
            
            String match = "Relation("+className;
            while(bloc.contains(match))
		{
                    //System.out.println("\n call out on class "+i);
                    call_out++;
                    bloc = bloc.replace(match, " ");
		}
            /*
            for(int j=0;j<nbClass;j++)
            {
                //System.out.println("\n still with class i : "+i+" and calling class j : "+j);
                if(j!=i)
                {
                    String bloc2=new String();
                    bloc2=(String)ApplyRefactoring.blocs.elementAt(j);
                    while(bloc2.contains("Relation("))
                    {
                        int d=bloc2.indexOf("Relation(");
			int f=bloc2.indexOf(");",d);
			String relation = bloc2.substring(d,f);
                        String source = relation.substring((relation.indexOf("(")+1),relation.indexOf(";"));
                        String target = relation.substring((relation.indexOf(",")+1),relation.lastIndexOf(","));
                        if (target.equals(className))
                        {
                            call_in ++ ;
                            System.out.println("\n call in on class "+i);
                        }
                        String s1=bloc2.substring(0,d);
			String s2=bloc2.substring(f,bloc2.length());
			bloc2=s1+s2;
                    }
                }
            }
            */
            if (call_out + call_in !=0)
            {
                //stability[i] = (double) call_out / (call_out + call_in) ;
                stability[i] = (double) call_out;
            }
            else
            {
                stability[i] = 0 ;
                //System.out.println("\n stability 0 on class "+i);
            }
            if (total_attributes[i] !=0)
            {
                direct_access[i] = (double) public_attributes[i]/total_attributes[i];
            }
            else
            {
                direct_access[i] = 0;
                //System.out.println("\n direct_access 0 on class "+i);
            }
            
            call_in = 0 ;
            call_out = 0 ;
	}       
        //System.out.println("\n total interfaces on system "+nbInterface);
         
        if (stability_metric)
        {
            objectives.add(average(stability));
            this.objectives_names.add("Sability");
        }
        
        if (deviance_metric)
        {
            objectives.add(standardDeviationCalculate(number));
            this.objectives_names.add("Standard Deviation");
        }
        
        if (encapsulation_metric)
        {
            objectives.add(average(direct_access));
            this.objectives_names.add("Encapsulation");
        }
        
        if (interfacing_metric)
        {
            // interface per classes ratio to be maximized so the inverse will be considered to have all objectives minimized
            objectives.add((double)((nbInterface/nbClass)));
            this.objectives_names.add("Interfacing");
        }
        
        
    }
    
    public static double standardDeviationCalculate( int[] data )
{
	final int n = data.length;
	if ( n < 2 )
	{
	return Double.NaN;
	}
	double avg = data[0];
	double sum = 0;
	for ( int i = 1; i < data.length; i++ )
	{
		double newavg = avg + ( data[i] - avg ) / ( i + 1 );
		sum += ( data[i] - avg ) * ( data [i] -newavg ) ;
		avg = newavg;
	}
	// Change to ( n - 1 ) to n if you have complete data instead of a sample.
	return Math.sqrt( sum / ( n - 1 ) );
}
    
    void print_solution()
    {
        System.out.println("\n --- Printing solution refactorings ---");
        System.out.println("\n --- Rank : "+this.rank);
        for(int i=0;i<this.refectorings.size();i++)
        {
            System.out.println(refactoring[this.refectorings.get(i)]);
        }
    }
    
    void print_metrics()
    {
        System.out.println("\n Rank :  "+this.rank+" Distance : "+this.distance+"\n");
        for(int i=0;i<this.objectives.size();i++)
        {
            System.out.print(" "+objectives_names.get(i)+" : "+this.objectives.get(i));
        }
        System.out.println("\n");
    }
    
    String objectives_names_to_string()
    {
        String result = " ";
        for(int i=0;i<this.objectives_names.size();i++)
        {
            result+=(String)objectives_names.get(i)+" ";
        }
        return result ;
    }
    
    String objectives_values_to_string()
    {
        String result = " ";
        for(int i=0;i<this.objectives.size();i++)
        {
            result+=Double.toString(objectives.get(i));
            
            if(i != (this.objectives.size()-1))
            {
                result+=" , ";
            }
            
        }
        return result ;
    }
    
    void mutation1()
    {
        // Number of refactorings to be mutated
        int sol = Random.random(0, 2);
        
        if (print_executed_refactorings) System.out.println("\n Mutations to be done : "+sol);
        
        for(int i=0;i<sol;i++)
        {
            int numRefactoring=Random.random(min_refactorings_interval, max_refactorings_interval);
            int position = Random.random(0, (this.refectorings.size()-1));
            if (print_executed_refactorings) System.out.println("\n Refactoring to be changed "+refactoring[this.refectorings.get(position)]+" by "+refactoring[numRefactoring]);
            this.refectorings.set(position, numRefactoring);
        } 
    }
    
    void mutation2()
    {
        int stop = 0 ;
        int position1 = 0 ;
        int position2 = 0 ;
        
        do
        {
            // Positions of refactorings to be rotated
            position1 = Random.random(0, (this.refectorings.size()-1));
            position2 = Random.random(0, (this.refectorings.size()-1));
            stop++;
        }
        while((position1==position2)&& (stop<200));
        
        if (stop == 200) {
            if (print_executed_refactorings) System.out.println("\n mutation2 failed to find two diffrent indexes! mutation aborted");
        }
        else
        {
            Integer temp = this.refectorings.get(position1);
            this.refectorings.set(position1, this.refectorings.get(position2));
            this.refectorings.set(position2, temp);
            if (print_executed_refactorings) System.out.println("\n mutation2 has switched between "+position1+" and "+position2);
        }
    }
    
    public double average(double[] nums) 
    {
        double result=0.0;
        int i=0;
        for(i=0; i < nums.length; i++)
        {
            result=result + nums[i];
        }
        return (double) result/nums.length;
    }
    
    public double average_int(int[] nums) 
    {
        double result=0.0;
        int i=0;
        for(i=0; i < nums.length; i++)
        {
            result=result + nums[i];
        }
        return (double) result/nums.length;
    }

    public static void main(String[] args) 
    {
        Solution test1 = new Solution();
        Solution test2 = new Solution();
        
        
        // test of create_solution_express
        if(true)
        {
            System.out.println("\n --- For Test1 ---");
            test1.create_solution_express();
            test1.print_metrics();
        
            System.out.println("\n --- For Test2 ---");
            test1.create_solution_express();
            test1.print_metrics();
        }
        
        // test of mutation1 and mutation2
        if(false)
        {
        
            System.out.println("\n --- Testing mutation1 ---");
            test1.create_solution();
            test1.print_solution();
            test1.mutation1();
            test1.print_solution();
        
            System.out.println("\n --- Testing mutation2 ---");
            test1.create_solution();
            test1.print_solution();
            test1.mutation2();
            test1.print_solution();
        }
        
        
    }
    
}

