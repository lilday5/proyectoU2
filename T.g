grammar T;

@header {
import java.util.ArrayList;
import java.util.List;
}

@members{   List <Tabla> tablas = new ArrayList<Tabla>();  
             Tabla tablaActual = null;
             int cont = 0;
}

inicio :  creacion usar tabla+ cerrar;

creacion : CREAR ID {System.out.println("CREATE DATABASE "+$ID.text); 
                    }; 

usar     : USAR  ID {System.out.println("USE DATABASE "+$ID.text); };


// tabla : TABLA ID INICIO {} campo+ FIN {};


tabla    : TABLA ID INICIO 
                  { 
                    //código para generar SQL
      System.out.println("CREATE TABLE "+$ID.text ); 
      System.out.println(" ("+$ID.text+"_key INTEGER AUTOINCREMENT NOT NULL"); 
                     //código para crear estructura de datos
                        Tabla t = new Tabla();
                        t.nombre =$ID.text;
                        tablas.add(t);
                        tablaActual = t;
                     //
                   }
             campo+ foranea*
             FIN  {
                     System.out.println("   );   "); 
                  };

campo   : ID  (t=NUMERICO | t=ALFABETICO | t=FECHA)
                { //aquí hay que agregar código para generar SQL
                    if(($t.text).compareTo("letras")==0) 
                       System.out.println(", "+$ID.text + " VARCHAR(300)" );
                    else if(($t.text).compareTo("fecha")==0)
                       System.out.println(", "+$ID.text + " DATE" ); 
                    else  System.out.println(", "+$ID.text + " " +$t.text );    

                 //el que sigue es código para crear estructura de datos
                  Atributo a  = new Atributo();
                      a.nombreAtributo = $ID.text;
                      a.tipoAtributo = $t.text;
                  tablaActual.atributos.add(a);
                };

foranea : FORANEA ID { 
                     for(int i=0; i<tablas.size()-1; i++){
                        if(($ID.text).compareTo(tablas.get(i).nombre)==0) {
                           if(tablas.get(i).nombre.compareTo(tablaActual.nombre)!=0) {
                              System.out.println(", "+$ID.text+"_key INTEGER" );
                              //código para crear estructura de datos
                              Atributo a  = new Atributo();
                              a.nombreAtributo = $ID.text+"_key";
                              a.tipoAtributo = "INTEGER";
                              tablaActual.atributos.add(a);
                              cont++;
                              break;
                           } else { System.out.println("No puede ser foranea la misma tabla "+$ID.text);
                              break;
                           }
                        }
                     }
                     if(cont==0) {
                        System.out.println("No existe la tabla "+$ID.text);
                     } 
                     cont =0;
                  };


cerrar   : CERRAR {
              for (int i=0; i<tablas.size(); i++){ 
                 System.out.println("\nTabla: "+tablas.get(i).nombre);
                 List <Atributo> atribs= tablas.get(i).atributos;
                 for (int j=0; j<atribs.size(); j++){  
                   System.out.print("<Atributo>  "+atribs.get(j).nombreAtributo);
                   System.out.println(" \t<TipoAtrib> "+atribs.get(j).tipoAtributo);
                 }
             }

           };

CERRAR   : 'cerrar';
NUMERICO  : 'numeros';
ALFABETICO: 'letras';
FECHA     : 'fecha'  ;
TABLA : 'tabla'  ;
FORANEA : 'depende_de';
INICIO: 'inicio' ;
FIN   : 'fin'    ;
USAR  : 'usar'   ;
CREAR : 'crear'  ;
ID    : ('a'..'z'|'A'..'Z'|'_') ('a'..'z'|'A'..'Z'|'0'..'9'|'_')* ;  
WS   :(' ' | '\n' | '\t' | '\r')+   {$channel=HIDDEN; } ;  

