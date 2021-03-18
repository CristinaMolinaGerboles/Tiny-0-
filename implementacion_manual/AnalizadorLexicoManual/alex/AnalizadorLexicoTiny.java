package alex;

import java.io.FileInputStream;
import java.io.Reader;

import alex.AnalizadorLexicoTiny.Estado;

import java.io.InputStreamReader;
import java.io.IOException;

public class AnalizadorLexicoTiny {

   private Reader input;
   private StringBuffer lex;
   private int sigCar;
   private int filaInicio;
   private int columnaInicio;
   private int filaActual;
   private int columnaActual;
   private static String NL = System.getProperty("line.separator");
   
   public static enum Estado {
    INICIO, REC_POR, REC_DIV, REC_PAP, REC_PCIERR, REC_PUNTOCOMA, REC_IGUAL, REC_MENORQ,
    REC_MENIG, REC_IGIG, REC_MAYIG, REC_MAYQ, REC_OR, REC_AND, REC_DAND, REC_DIF1, REC_DIFF,     
    REC_ENTEROS, REC_MAS, REC_MENOS, REC_CERO, REC_DECIMAL, REC_EXP, REC_FINALDEC, REC_TRAMPA,
    REC_ZERO, REC_SIGNO, REC_FINALEXP, REC_VARIABLE, REC_ANDAND, REC_EOF
   }

   private Estado estado;

   public AnalizadorLexicoTiny(Reader input) throws IOException {
    this.input = input;
    lex = new StringBuffer();
    sigCar = input.read();
    filaActual=1;
    columnaActual=1;
   }
   
   public UnidadLexica sigToken() throws IOException {
     estado = Estado.INICIO;
     filaInicio = filaActual;
     columnaInicio = columnaActual;
     lex.delete(0,lex.length());
     while(true) {
         switch(estado) {
           case INICIO: 
              if(hayLetra())  transita(Estado.REC_VARIABLE);
              else if (hayNot()) transita(Estado.REC_DIF1);
              else if (hayIgual()) transita(Estado.REC_IGUAL);
              else if(haySuma())  transita(Estado.REC_MAS);
              else if(hayResta())  transita(Estado.REC_MENOS);
              else if (hayDigitoPos()) transita(Estado.REC_ENTEROS);
              else if (hayCero()) transita(Estado.REC_CERO);
              else if (hayAnd()) transita(Estado.REC_AND);
              else if (hayMul()) transita(Estado.REC_POR);
              else if (hayDiv()) transita(Estado.REC_DIV);
              else if (hayPAp()) transita(Estado.REC_PAP);
              else if (hayPCierre()) transita(Estado.REC_PCIERR);
              else if (hayPuntoComa()) transita(Estado.REC_PUNTOCOMA);
              else if (hayMenor()) transita(Estado.REC_MENORQ);
              else if (hayMayor()) transita(Estado.REC_MAYQ);
              else if (haySep()) transitaIgnorando(Estado.INICIO);
              else if (hayEOF()) transita(Estado.REC_EOF);
              else error();
              break;
           case REC_VARIABLE:
              if (hayLetra() || hayDigito() || haybarrabaja()) transita(Estado.REC_VARIABLE);
              else return unidadVar();               
              break;
                       	   
           case REC_DIF1:
        	   if (hayIgual()) transita (Estado.REC_DIFF);
        	   else error();
        	   break;
           case REC_DIFF: return unidadDiff();
           
           case REC_ENTEROS:	
               if (hayDigito()) transita(Estado.REC_ENTEROS);	
               else if (hayPunto()) transita(Estado.REC_DECIMAL);	
               else if (haye()) transita (Estado.REC_EXP);	
               else return unidadEnt();	
               break;	
           case REC_EXP:	
        	   if (hayCero()) transita(Estado.REC_ZERO);	
        	   else if (haySuma()) transita(Estado.REC_SIGNO);	
        	   else if (hayResta()) transita(Estado.REC_SIGNO);	
        	   else if (hayDigitoPos()) transita(Estado.REC_FINALEXP);	
        	   else error();	
        	   break;
           case REC_DECIMAL: 
        	   if (hayDigito()) transita(Estado.REC_FINALDEC);
        	   else error();
               break;
           case REC_FINALDEC:
        	   if (hayDigitoPos()) transita(Estado.REC_FINALDEC);
        	   else if (hayCero()) transita(Estado.REC_TRAMPA);
        	   else if (haye()) transita(Estado.REC_EXP);
        	   else return unidadReal();
        	   break;
        	   
           case REC_TRAMPA:	
        	   if (hayDigitoPos()) transita(Estado.REC_FINALDEC);	
        	   else if (hayCero()) transita(Estado.REC_TRAMPA);	
        	   else error();	
        	   break;	
           case REC_SIGNO:	
        	   if (hayDigitoPos()) transita(Estado.REC_FINALEXP);	
        	   else if (hayCero()) transita(Estado.REC_ZERO);	
        	   else error();	
        	   break;	
           case REC_FINALEXP:	
        	   if (hayDigito()) transita(Estado.REC_FINALEXP);	
        	   else return unidadReal();	
        	   break;
        	   
               
           case REC_IGUAL:
        	   if (hayIgual()) transita (Estado.REC_IGIG);
        	   else return unidadIgual();        	  
        	   break;
           case REC_IGIG: return unidadIgIg();
                  	  
           case REC_AND: 
        	   if (hayAnd()) transita(Estado.REC_ANDAND);
        	   else error();
        	   break;
        	   
           case REC_CERO:
               if (hayPunto()) transita(Estado.REC_DECIMAL);
               else if (hayDigitoPos()||hayCero()||hayLetra()) error();
               else return unidadEnt();
               break; 
           case REC_ZERO:
        	   return unidadReal();
           case REC_MAS:
               if (hayDigitoPos()) transita(Estado.REC_ENTEROS);
               else if(hayCero()) transita(Estado.REC_CERO);
               else return unidadMas();
               break;
           case REC_MENOS: 
               if (hayDigitoPos()) transita(Estado.REC_ENTEROS);
               else if(hayCero()) transita(Estado.REC_CERO);
               else return unidadMenos();
               break;
        	   
           case REC_ANDAND: return unidadAndAnd();
           case REC_POR: return unidadPor();
           case REC_DIV: return unidadDiv();              
           case REC_PAP: return unidadPAp();
           case REC_PCIERR: return unidadPCierre();
           case REC_PUNTOCOMA: return unidadPuntoComa();
           case REC_EOF: return unidadEof();
           
           case REC_MENORQ:
        	   if (hayIgual()) transita(Estado.REC_MENIG);
        	   else return unidadMenor();
        	   break;
           case REC_MENIG: return unidadMenIg();
           
           case REC_MAYQ:
        	   if (hayIgual()) transita(Estado.REC_MAYIG);
        	   else return unidadMayor();
        	   break; 
        	   
           case REC_MAYIG: return unidadMayIg();
         }
     }    
   }
   private void transita(Estado sig) throws IOException {
     lex.append((char)sigCar);
     sigCar();         
     estado = sig;
   }
   private void transitaIgnorando(Estado sig) throws IOException {
     sigCar();         
     filaInicio = filaActual;
     columnaInicio = columnaActual;
     estado = sig;
   }
   private void sigCar() throws IOException {
     sigCar = input.read();
     if (sigCar == NL.charAt(0)) saltaFinDeLinea();
     if (sigCar == '\n') {
        filaActual++;
        columnaActual=0;
     }
     else {
       columnaActual++;  
     }
   }
   private void saltaFinDeLinea() throws IOException {
      for (int i=1; i < NL.length(); i++) {
          sigCar = input.read();
          if (sigCar != NL.charAt(i)) error();
      }
      sigCar = '\n';
   }
   
   private boolean hayLetra() {return sigCar >= 'a' && sigCar <= 'z' ||
                                      sigCar >= 'A' && sigCar <= 'Z';}
   
   
   private boolean haySuma() {return sigCar == '+';}	
   private boolean hayResta() {return sigCar == '-';}
   
   private boolean haye() {return sigCar == 'e'||sigCar == 'E';}
  
 
   
   private boolean hayDigitoPos() {return sigCar >= '1' && sigCar <= '9';}
   private boolean haybarrabaja() {return sigCar == '_';}
   
   private boolean hayCero() {return sigCar == '0';}
   private boolean hayDigito() {return hayDigitoPos() || hayCero();}
   private boolean hayMul() {return sigCar == '*';}
   private boolean hayDiv() {return sigCar == '/';}
   private boolean hayAnd() {return sigCar == '&';}
   private boolean hayPAp() {return sigCar == '(';}
   private boolean hayPCierre() {return sigCar == ')';}
   private boolean hayIgual() {return sigCar == '=';}
   private boolean hayPuntoComa() {return sigCar == ';';}
   private boolean haySep() {return sigCar == ' ' || sigCar == '\t' || sigCar=='\n';}
   private boolean hayNL() {return sigCar == '\r' || sigCar == '\b' || sigCar == '\n';}
   private boolean hayEOF() {return sigCar == -1;}
   private boolean hayNot() {return sigCar == '!';}
   private boolean hayPunto() {return sigCar == '.';}
   private boolean hayMenor() {return sigCar == '<';}
   private boolean hayMayor() {return sigCar == '>';}

   
   
   private UnidadLexica unidadVar() {
     switch(lex.toString()) {
         case "false":  
            return new UnidadLexicaUnivaluada(filaInicio,columnaInicio,ClaseLexica.FALSE);
         case "true":    
            return new UnidadLexicaUnivaluada(filaInicio,columnaInicio,ClaseLexica.TRUE);
         case "bool":
        	 return new UnidadLexicaUnivaluada (filaInicio, columnaInicio, ClaseLexica.BOOL);
         case "real":
        	 return  new UnidadLexicaUnivaluada (filaInicio, columnaInicio, ClaseLexica.REAL);
         case "int":
        	 return new UnidadLexicaUnivaluada (filaInicio, columnaInicio, ClaseLexica.INT);
         case "not":
        	 return new UnidadLexicaUnivaluada (filaInicio, columnaInicio, ClaseLexica.NOT);
         case "and":
        	 return new UnidadLexicaUnivaluada (filaInicio, columnaInicio, ClaseLexica.AND);
         case "or":
        	 return new UnidadLexicaUnivaluada (filaInicio, columnaInicio, ClaseLexica.OR);
         default:    
            return new UnidadLexicaMultivaluada(filaInicio,columnaInicio,ClaseLexica.VAR,lex.toString());     
      }
   }  
   private UnidadLexica unidadEnt() {	
	     return new UnidadLexicaMultivaluada(filaInicio,columnaInicio,ClaseLexica.INT,lex.toString());     	
   }    	
   private UnidadLexica unidadReal() {	
     return new UnidadLexicaMultivaluada(filaInicio,columnaInicio,ClaseLexica.REAL,lex.toString());     	
   }     
   private UnidadLexica unidadPor() {
     return new UnidadLexicaUnivaluada(filaInicio,columnaInicio,ClaseLexica.POR);     
   }    
   private UnidadLexica unidadDiv() {
     return new UnidadLexicaUnivaluada(filaInicio,columnaInicio,ClaseLexica.DIV);     
   }    
   private UnidadLexica unidadPAp() {
     return new UnidadLexicaUnivaluada(filaInicio,columnaInicio,ClaseLexica.PAP);     
   }    
   private UnidadLexica unidadPCierre() {
     return new UnidadLexicaUnivaluada(filaInicio,columnaInicio,ClaseLexica.PCIERRE);     
   }
   private UnidadLexica unidadMas() {
	     return new UnidadLexicaUnivaluada(filaInicio,columnaInicio,ClaseLexica.MAS);     
	   }    
	   private UnidadLexica unidadMenos() {
	     return new UnidadLexicaUnivaluada(filaInicio,columnaInicio,ClaseLexica.MENOS);     
	   }    
   private UnidadLexica unidadIgual() {
     return new UnidadLexicaUnivaluada(filaInicio,columnaInicio,ClaseLexica.IGUAL);     
   }    
   private UnidadLexica unidadPuntoComa() {
     return new UnidadLexicaUnivaluada(filaInicio,columnaInicio,ClaseLexica.PUNTOCOMA);     
   }    
   private UnidadLexica unidadEof() {
     return new UnidadLexicaUnivaluada(filaInicio,columnaInicio,ClaseLexica.EOF);     
   }
   private UnidadLexica unidadDiff() {
	   return new UnidadLexicaUnivaluada(filaInicio,columnaInicio,ClaseLexica.DIFF);
   }
   private UnidadLexica unidadIgIg() {
	   return new UnidadLexicaUnivaluada(filaInicio,columnaInicio,ClaseLexica.IGIG);
   }
   private UnidadLexica unidadAndAnd() {
	   return new UnidadLexicaUnivaluada(filaInicio,columnaInicio,ClaseLexica.ANDAND);
   }
   private UnidadLexica unidadMenor() {
	   return new UnidadLexicaUnivaluada(filaInicio,columnaInicio,ClaseLexica.MENOR);
   }
   private UnidadLexica unidadMenIg() {
	   return new UnidadLexicaUnivaluada(filaInicio,columnaInicio,ClaseLexica.MENIG);
   }
   private UnidadLexica unidadMayor() {
	   return new UnidadLexicaUnivaluada(filaInicio,columnaInicio,ClaseLexica.MAYOR);
   }
   private UnidadLexica unidadMayIg() {
	   return new UnidadLexicaUnivaluada(filaInicio,columnaInicio,ClaseLexica.MAYIG);
   }
   
   private void error() {
     System.err.println("("+filaActual+','+columnaActual+"):Caracter inesperado");  
     System.exit(1);
   }

   public static void main(String arg[]) throws IOException {
     Reader input = new InputStreamReader(new FileInputStream("input.txt"));
     AnalizadorLexicoTiny al = new AnalizadorLexicoTiny(input);
     UnidadLexica unidad;
     do {
       unidad = al.sigToken();
       System.out.println(unidad);
     }
     while (unidad.clase() != ClaseLexica.EOF);
    } 
}