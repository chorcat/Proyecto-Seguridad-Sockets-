
import java.io.*;
import java.security.*;
import javax.crypto.*;
import javax.crypto.spec.*;
import java.security.AlgorithmParameters;

// IAIK librerias
//import iaik.security.provider.IAIK;
//import iaik.utils.CryptoUtils;


public class CifradoSimetrico {

	public CifradoSimetrico() {
		super();
		// TODO Auto-generated constructor stub
	}
	public static void cifrar(String ficheroaux,String ruta,String nombrecifrado,String algoritmo,int id) throws Exception {

	String provider         = "SunJCE";
        String directorio=ruta;
        
	FileInputStream  ftextoclaro      = new FileInputStream (directorio+ficheroaux);
	FileOutputStream ftextocifrado = new FileOutputStream(directorio+nombrecifrado);
	FileOutputStream fparametros   = new FileOutputStream(directorio+"parametros"+id);
	FileOutputStream fclave            = new FileOutputStream(directorio+"fclaveks"+id);
	FileInputStream  fclave_in        = new FileInputStream (directorio+"fclaveks"+id);
	
	BufferedWriter   fmedidas         = new BufferedWriter(new FileWriter(directorio+"medidasCifrado"+id));
	    
	byte   bloqueclaro[]    = new byte[2024];

        	byte   bloquecifrado[]  = new byte[2048];
        	//String algoritmo        = "AES";
        	String transformacion   = "/CBC/PKCS5Padding";
        	int    longclave        = 128;
        	int    longbloque;
        	int    i;
        	double t, tbi,tbf;
        	double lf;              // longitud del fichero
		   
 
       // if (provider.equals("IAIK")) { IAIK.addAsProvider(); }  
          
        
        /************** Generar clave **********************/
        KeyGenerator  kgen     = KeyGenerator.getInstance(algoritmo);
        kgen.init(longclave);
        SecretKey     skey     = kgen.generateKey();
        
        byte[]        skey_raw      = skey.getEncoded();
        fclave.write(skey_raw);
        fclave.close();
        
        fclave_in.read(skey_raw);
        SecretKeySpec ks = new SecretKeySpec(skey_raw, algoritmo);
        
                
        //*******************************************************************
        //               Cifrar
        //*******************************************************************
        System.out.println("*** INICIO CIFRADO " + algoritmo + "-" + longclave + " ************");
		
        Cipher cifrador = Cipher.getInstance(algoritmo + transformacion);
        
        cifrador.init(Cipher.ENCRYPT_MODE, ks);
        
        
        i = 0;
        t = 0;
        lf = 0;
        tbi = 0;
        tbf = 0;

        while ((longbloque = ftextoclaro.read(bloqueclaro)) > 0) {
               i++;
               
               lf = lf + longbloque;
               
               tbi = System.nanoTime();
               bloquecifrado = cifrador.update(bloqueclaro,0,longbloque);
               tbf = System.nanoTime();

               t = t + tbf - tbi;
               fmedidas.write("T iteracion " +  i  + " = " + (tbf-tbi) + " nanoseg \n");         
               
               ftextocifrado.write(bloquecifrado);
        }     
        
        // Hacer dofinal y medir su tiempo
        tbi = System.nanoTime();
        bloquecifrado = cifrador.doFinal();
        tbf = System.nanoTime();

        t = t + tbf - tbi;

        ftextocifrado.write(bloquecifrado);
        
        // Escribir resultados
        
        //System.out.println("Long. ultimo bloque" + bloquecifrado.length );
        System.out.println("*** FIN CIFRADO " + algoritmo + "-" + longclave + " Provider: " + provider);
        System.out.println("Bytes  cifrados = " + (int)lf );
        System.out.println("Tiempo cifrado  = " + t/1000000 + " mseg");
        System.out.println("Velocidad       = " + (lf*8*1000)/ t + " Mpbs");

        // Cerrar ficheros
 	    ftextocifrado.close();
        ftextoclaro.close();
        fmedidas.close();
	
        /*******************************************************************
        *  Obtener parametros del algoritmo y archivarlos
        *  
        *  NOTA: Para los cifradores en flujo no se ejecuta el lazo de  
        *        parametros porque no se necesitan. Ejemplo: RC4
        *******************************************************************/
        // System.out.println("Leer los parametros(IV,...) usados por el cifrador ..." );

        //AlgorithmParameters  paramxx =  cifrador.getParameters();

        if (provider.equals("SunJCE") &&
                ( algoritmo.equals("AES")                    ||
        		  algoritmo.equals("Blowfish")               ||
        		  algoritmo.equals("DES")                    ||
        		  algoritmo.equals("DESede")                 ||
        		  algoritmo.equals("DiffieHellman")          ||
        		  algoritmo.equals("OAEP")                   ||
        		  algoritmo.equals("PBEWithMD5AndDES")       ||
        		  algoritmo.equals("PBEWithMD5AndTripleDES") ||
        		  algoritmo.equals("PBEWithSHA1AndDESede")   ||
        		  algoritmo.equals("PBEWithSHA1AndRC2_40")   ||
        		  algoritmo.equals("RC2")
        		  )
            ||  (provider.equals("IAIK") && (! algoritmo.equals("RC4")))  )
 
        {
        		 AlgorithmParameters param = AlgorithmParameters.getInstance(algoritmo);        
        		 param =  cifrador.getParameters();
        		
        		 System.out.println("Parametros del cifrado ..." + param.toString());
        		 byte[]  paramSerializados = param.getEncoded();
        		 fparametros.write(paramSerializados);
        		 fparametros.close();
        		        				
        };
        	
	}         
        //*****************************************************************************
        //					DESCIFRAR
        //*****************************************************************************
	public static void descifrar(String ficheroaux,String ruta,String nombrecifrado,String algoritmo,int id) throws Exception{
	String directorio=ruta;
		FileInputStream  ftextocifrado2 = new FileInputStream (directorio+nombrecifrado);
	FileOutputStream ftextoclaro2   = new FileOutputStream(directorio+ficheroaux);
	FileInputStream  fparametros_in = new FileInputStream (directorio+"parametros"+id);
	FileInputStream  fclave_in        = new FileInputStream (directorio+"fclaveks"+id);
	 byte[]        skey_raw =FiletoArray(fclave_in);
	 //fclave_in.read(skey_raw);
     SecretKeySpec ks = new SecretKeySpec(skey_raw, algoritmo);
	String transformacion   = "/CBC/PKCS5Padding";
	String provider         = "SunJCE";
	int longbloque;

        byte bloquecifrado2[]  = new byte[1024];
        byte bloqueclaro2[]    = new byte[1048];
		
        System.out.println("*************** INICIO DESCIFRADO *****************" );
        
        Cipher descifrador = Cipher.getInstance(algoritmo + transformacion, provider);

        // Leer los parametros si el algoritmo soporta parametros
        if (provider.equals("SunJCE") &&
                ( algoritmo.equals("AES")                    ||
        		  algoritmo.equals("Blowfish")               ||
        		  algoritmo.equals("DES")                    ||
        		  algoritmo.equals("DESede")                 ||
        		  algoritmo.equals("DiffieHellman")          ||
        		  algoritmo.equals("OAEP")                   ||
        		  algoritmo.equals("PBEWithMD5AndDES")       ||
        		  algoritmo.equals("PBEWithMD5AndTripleDES") ||
        		  algoritmo.equals("PBEWithSHA1AndDESede")   ||
        		  algoritmo.equals("PBEWithSHA1AndRC2_40")   ||
        		  algoritmo.equals("RC2")
        		  )
        	 ||  (provider.equals("IAIK") && (! algoritmo.equals("RC4")))        			
   			 )
 
        {
            AlgorithmParameters params = AlgorithmParameters.getInstance(algoritmo,provider);        
            byte[] paramSerializados = new byte[fparametros_in.available()];

            fparametros_in.read(paramSerializados);         
            params.init(paramSerializados);
            
            System.out.println("Parametros del descifrado ... = " + params.toString());

            descifrador.init(Cipher.DECRYPT_MODE, ks, params);
        }
        else
        {
        	descifrador.init(Cipher.DECRYPT_MODE, ks);
        }
        

        while ((longbloque = ftextocifrado2.read(bloquecifrado2)) > 0) {
            
              bloqueclaro2 = descifrador.update(bloquecifrado2,0,longbloque);
     	   
             //System.out.println("Longitud cifrado " + bloquecifrado.length );
             ftextoclaro2.write(bloqueclaro2);
        }

        bloqueclaro2 = descifrador.doFinal();
 	    ftextoclaro2.write(bloqueclaro2);
        
 	    ftextocifrado2.close();
        ftextoclaro2.close();

        System.out.println("*************** FIN DESCIFRADO *****************" );
       
	}	
	static byte[]  FiletoArray(FileInputStream fis) throws  IOException {
	      
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        try {
            for (int readNum; (readNum = fis.read(buf)) != -1;) {
                bos.write(buf, 0, readNum); //no doubt here is 
              
            }
        } catch (IOException ex) {
            
        }
        byte[] bytes = bos.toByteArray();
        return bytes;
        //below is the different part
     
    }
}

