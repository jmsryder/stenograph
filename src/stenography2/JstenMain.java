package stenography2;


//import various packages

import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import javax.imageio.ImageIO;


public class JstenMain {
	
	// define the location of the BMP file, on the local file system
  
	final static String input_path = System.getProperty("user.dir") + File.separator + "input_files";
	final static String output_path = System.getProperty("user.dir") + File.separator + "output_files";

	
	// ------------------------------------------------------------------------------
   
   
	public static void main(String args[]){

		// this is the main sequence
		
		createDirectory("input_files");
		createDirectory("output_files");

		Scanner reader = new Scanner(System.in);  // Reading from System.in
		System.out.println("Hello - please select an option: ");
		System.out.println("1) Encode a messaage in an image ");
		System.out.println("2) Decode a messaage from an image ");
		int choice1 = reader.nextInt();	
		
		
		
		if (choice1 == 1) {
			System.out.println("*** encode a file ***");
			System.out.println("please type the name of a BMP image file, located in " +  input_path);
			String image_file_in2 = reader.next();
			String image_file_in = input_path + "/" + image_file_in2;	

			System.out.println("please type the name of the file to be coded, located in " +  input_path);
			System.out.println("note that the filename (including extension) must be less than 20 characters, and not start with a zero");
			String secret_file_in2 = reader.next();
			String secret_file_in = input_path + "/" + secret_file_in2;
    		     		  
			// 1) read in the BMP file
			
			System.out.println("image_file_in: " + image_file_in);
			System.out.println("image_file_in: " + secret_file_in2);

			
			BufferedImage image_orig = readImageFile(image_file_in);
			final int width = image_orig.getWidth();
			final int height = image_orig.getHeight();
    			

			
			// 2) read in the byte data of the image
			byte image_bytes[]  = imageToBytes(image_orig);
			int image_bytes_length = image_bytes.length;
			System.out.println("** image_bytes_length: " +  image_bytes_length);

			// 3) read in the secret file data
			byte[] secret_data_bytes2 = null;
			byte secret_data_bytes3[] = readSecretFile(secret_data_bytes2, secret_file_in, secret_file_in2);
    		   

			
			int text_bytes_length = secret_data_bytes3.length;		
			System.out.println("** text_length: " +  text_bytes_length);
    		   
			// 4) image file has to be eight times larger than the secret file (as we are hiding it in the least significant bit)
			if ((text_bytes_length + 10 + 20) * 8 >= image_bytes_length) {
				System.out.println("** encoding is not possible, as the image file is too small");
				System.out.println("** We need an image file of at least" + ((text_bytes_length + 10 + 20) * 8) + "bytes");
				System.out.println("Exiting - please run the program again");    		
				System.exit(0);
			} else  {
				System.out.println("** the image file is large enough");
			}    
    		       		   
			// 5) Hide the file in the image
			byte new_image_bytes[] = encodeFile(image_bytes, secret_data_bytes3, image_orig);    		  
    			  
			// 6) Create the new RGB image
			BufferedImage image = createRGBImage(new_image_bytes, width, height);
   			   
			File outputfile2 = new File(output_path + "/" + "image_encoded.bmp");

			try {
				ImageIO.write(image, "BMP", outputfile2);
				System.out.println("process successful");
				System.out.println("Coded image saved as 'image_encoded.BMP', in the directory: " +  output_path);   				   				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("problem writing image");
				e.printStackTrace();
    			}    	     	
    	     	
    	     	
    		
			} else if (choice1 == 2) { 
        	
				System.out.println("*** decode a secret file from an image ***");
				System.out.println("please type the name of a BMP image file containing the secret file");
				System.out.println("the file should be located in the following directory: " +  input_path);
				String image_file_in = reader.next();
				image_file_in = input_path + "/" + image_file_in;	

				System.out.println("image_file_in: " + image_file_in);

				
				// 1) read in the BMP file
				BufferedImage image_encoded = readImageFile(image_file_in);
  			
				// 2) read in the file length info from the image first of all
				byte new_image_bytes[]  = imageToBytes(image_encoded);
				byte new_secret_file_bytes_prep[] = decodeFile(new_image_bytes, (10*8));
   		   	  		  
				String secret_file_length = "";

				for (int jcount = 0; jcount < 20; jcount +=1) {	    
					secret_file_length += (char)Integer.parseInt(Integer.toBinaryString(new_secret_file_bytes_prep[jcount]), 2);			
				}
    		   
				String secret_filename = "";
				String secret_file_body = "";
    		    
				secret_file_length = secret_file_length.replaceFirst("^0+(?!$)", "");
    		   
				int secret_file_length_int = Integer.parseInt(secret_file_length.trim());

				// 3) read in all the hidden information    		
				byte new_secret_file_bytes[] = decodeFile(new_image_bytes, ((10 + 20 + secret_file_length_int) * 8));

				// 4) extract the original file name    		
				for (int jcount = 10; jcount < 30; jcount +=1) {	    
					secret_filename += (char)Integer.parseInt(Integer.toBinaryString(new_secret_file_bytes[jcount]), 2);			
				}
    		   
				secret_filename = secret_filename.replaceFirst("^0+(?!$)", "");
				secret_filename = output_path + "/" + secret_filename;

				// 4) extract everything else   		
				for (int jcount = 30; jcount < (10 + 20 + secret_file_length_int); jcount +=1) {	    
					secret_file_body += (char)Integer.parseInt(Integer.toBinaryString(new_secret_file_bytes[jcount]), 2);			
				}
    		   
				System.out.println("secret_filename: " + secret_filename );		

				writeStringToFile(secret_file_body, secret_filename);   		
    		
    		    		
				} else  {
					System.out.println("That's not one of the options!");
					System.out.println("Exiting - please run the program again");    		
					System.exit(0);
				}
	   
   }  // end of the main routine

	// ------------------------------------------------------------------------------

	public static void createDirectory(String directory_name) {
		{

			Path path = Paths.get(System.getProperty("user.dir") + File.separator + directory_name);
			//if directory already exists
			if (!Files.exists(path)) {
				try {
					Files.createDirectories(path);
				} catch (IOException e) {
					//fail to create directory
					e.printStackTrace();
				}
			}

		}

	}
	
	// ------------------------------------------------------------------------------
	
	public static void writeStringToFile(String secret_file_out, String filename_out) {
		
			try {
				File file = new File(filename_out);
				FileWriter fileWriter = new FileWriter(file);
				fileWriter.write(secret_file_out);
				fileWriter.flush();
				fileWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
	}
	
	// ------------------------------------------------------------------------------
		
	public static BufferedImage readImageFile(String fileName) {
	    	BufferedImage img = null;{	    		
	    		try {
	    			img = ImageIO.read(new File(fileName));
	    			System.out.println("The BMP image has been read");
	    		} catch (IOException e) {
	    			System.out.println("The BMP image has not been read");
	    			System.out.println("Please check that it is, indeed an BMP image, and try again");
	     			System.exit(0);
	    		}	    		
	    		}
			return img;				
	}  // end of read image routine
	
	// ------------------------------------------------------------------------------
			
	public String hexToBin(String s) {
		  return new BigInteger(s, 16).toString(2);
	} // end of convert hex string to binary

	// ------------------------------------------------------------------------------
			
	public static byte[] imageToBytes(BufferedImage image)
	{
		WritableRaster raster = image.getRaster();
		DataBufferByte buffer = (DataBufferByte)raster.getDataBuffer();
		return buffer.getData();
	}  // end of convert image to bytes
		
	// ------------------------------------------------------------------------------	
	
	public static BufferedImage createRGBImage(byte[] bytes, int width, int height) {
	    DataBufferByte buffer = new DataBufferByte(bytes, bytes.length);
	    ColorModel cm = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB), new int[]{8, 8, 8}, false, false, Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
	    return new BufferedImage(cm, Raster.createInterleavedRaster(buffer, width, height, width * 3, 3, new int[]{2, 1, 0}, null), false, null);	
	}
	
	// ------------------------------------------------------------------------------	
		
	public static byte[] encodeFile(byte[] image_bytes, byte[] secret_data_bytes, BufferedImage image_orig) {
		      
  		final boolean hasAlphaChannel = image_orig.getAlphaRaster() != null;

		if (hasAlphaChannel) {
		    	  
			System.out.println("BMP Image has an alpha channel");
	   		System.out.println("Exiting - please run the program again using a BMP image without an alpha channel");    		
	   		System.out.println("(... upgrade to come in the next version)");    		
	   		System.exit(0);
			
		    } else {
		    	  
		    System.out.println("Image does not have an alpha channel"); 		    	  		         
			System.out.println("secret_file_bytes.length: " + secret_data_bytes.length);
		         
		    for (int pixel_count = 0; pixel_count < secret_data_bytes.length; pixel_count +=1) {

			    for (int message_bit_count = 7; message_bit_count > -1; message_bit_count -=1) {
			        int position = message_bit_count;			        	 
			        int value2 = ((secret_data_bytes[pixel_count] >> position) & 1);	 		        	 
			        int pigeon_hole = (pixel_count * 8) + (7-message_bit_count);
				  	changeBit(image_bytes, pigeon_hole, 0, value2);				  	
			    }  // end of message_bit_count
			} // end of pixel_count 		         		         
		}
		return image_bytes;
	}	
	
	// ------------------------------------------------------------------------------
		   
	public static byte[] decodeFile(byte[] new_image_bytes, int file_string_length) {

		int byteLocation2 = 0;
		int bitLocation2 = 0;
		           
		byte[] new_secret_file_bytes = new byte[file_string_length];
		for (int pixel_count = 0; pixel_count < file_string_length; pixel_count +=1) {
		        	 					
			if (pixel_count % 8 == 0 && !(pixel_count == 0)) {
				byteLocation2++;	
				bitLocation2 = 0;
			} // end of bit loop
					        	 
			int message_bin = readBit(new_image_bytes, 0, pixel_count);
			writeBit(new_secret_file_bytes, message_bin, bitLocation2, byteLocation2);			
			bitLocation2++;			
		}
		return new_secret_file_bytes;	 
		         	         
	} // end of decodeFile
   	   
	// ------------------------------------------------------------------------------	   
	   	
	public static void changeBit(byte[] input, int byteLocation, int bitLocation, int value) {
		   
		byte tempByte = input[byteLocation];
		
		if (value == 0) 
			tempByte = (byte) (tempByte & ~(1 << bitLocation));
		else
			tempByte = (byte) (tempByte | (1 << bitLocation));		    		  	
			
		input[byteLocation] = tempByte;
		
	}  // end of changeBit
	
	// ------------------------------------------------------------------------------
	   
	public static int readBit(byte[] input, int bitLocation, int value) {
		   		   
		int bitValue = ((byte)input[value]) & (0x01 << 0) ;;		    
		return bitValue;
		
	}  // end of readBit	   
	   
	// ------------------------------------------------------------------------------
		   
	public static void writeBit(byte[] output, int input, int bitLocation, int byteLocation) {
			 
		byte tempByte = output[byteLocation];

		if (input == 0) 
			tempByte = (byte) (tempByte & ~(1 << (7-bitLocation)));
		else
			tempByte = (byte) (tempByte | (1 << (7-bitLocation)));
		    		  	
		output[byteLocation] = tempByte;		    
		  			  	
	}  // end of writeBit	   	   	   
	
	 // ------------------------------------------------------------------------------
	
	public static byte[] readSecretFile(byte[] secret_data_bytes2, String secret_file_in, String secret_filename) {
		
		try {	

			// 1) read in the secret file
			Path secret_file_path = Paths.get(secret_file_in);

			secret_data_bytes2 = Files.readAllBytes(secret_file_path);
			System.out.println("Successfully read from secret file");

			} catch (IOException ioe) {
					System.out.println("Trouble reading from the file: " + ioe.getMessage());
			}
		
		// 2) read in secret file length
		int text_length = secret_data_bytes2.length;		

		// 3) add zeros in front of file length
		String text_length_string = String.format("%010d", text_length);					
	   
		// 4) convert file length string to bytes, and merge with filename string (to add to the image)		
		String filename_string = ("00000000000000000000" + secret_filename).substring(secret_filename.length());
		String secret_precursors = text_length_string + filename_string;	    
		byte secret_precursors_bytes[] = secret_precursors.getBytes();		   
		byte[] combined = new byte[secret_precursors_bytes.length + secret_data_bytes2.length];

		for (int i = 0; i < combined.length; ++i)
		{
			combined[i] = i < secret_precursors_bytes.length ? 
				secret_precursors_bytes[i] : secret_data_bytes2[i - secret_precursors_bytes.length];
		}	   
	     
		byte secret_data_bytes4[] = combined; 
	   	   
	return secret_data_bytes4; 
	    
}	
	
}	