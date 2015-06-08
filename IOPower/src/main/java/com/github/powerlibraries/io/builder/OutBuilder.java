package com.github.powerlibraries.io.builder;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.annotation.AnnotationFormatError;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.ZipOutputStream;

import com.github.powerlibraries.io.builder.targets.Target;
import com.github.powerlibraries.io.helper.CompressorRegistry;

/**
 * This builder is used to create an output chain.
 * 
 * @author Manuel Hegner
 *
 */
public class OutBuilder {
	private Target target;
	private boolean compress=false;
	private Base64.Encoder base64Encoder=null;

	public OutBuilder(Target target) {
		this.target=target;
	}
	
	/**
	 * This method will tell the builder to compress the bytes. The returned writer or stream will contain an appropriate
	 * compressor. If the defined target specifies a name with a file ending, the builder will try to 
	 * use the appropriate compressor for the file extension. If there is no extension or it is unknown this
	 * simply adds a {@link DeflaterOutputStream} to the chain.
	 * 
	 * If you want to add file extensions to the automatic selection see {@link CompressorRegistry#registerWrapper}.
	 * @return this builder
	 */
	public OutBuilder compress() {
		compress=true;
		return this;
	}
	
	/**
	 * This method will add a {@link Base64.Encoder} to this chain.
	 * @return this builder
	 */
	public OutBuilder encodeBase64() {
		base64Encoder=Base64.getEncoder();
		return this;
	}
	
	/**
	 * This method will add a {@link Base64.Encoder} to this chain.
	 * @param encoder the specific Base64 encoder that should be used.
	 * @return this builder
	 */
	public OutBuilder encodeBase64(Base64.Encoder encoder) {
		base64Encoder=encoder;
		return this;
	}
	
	/**
	 * This method creates a simple {@link OutputStream} from this builder with all the chosen options.
	 * @return an {@link OutputStream}
	 * @throws IOException if any element of the chain throws an {@link IOException}
	 */
	public OutputStream fromStream() throws IOException {
		return createOutputStream();
	}
	
	/**
	 * This method creates a {@link BufferedWriter} from this builder with all the chosen options. It uses the default 
	 * {@link Charset} for that.
	 * @return a {@link BufferedWriter}
	 * @throws IOException if any element of the chain throws an {@link IOException}
	 */
	public BufferedWriter fromWriter() throws IOException {
		return new BufferedWriter(new OutputStreamWriter(createOutputStream()));
	}
	
	/**
	 * This method creates a {@link BufferedWriter} from this builder with all the chosen options.
	 * @param charset the charset used to convert the characters into bytes
	 * @return a {@link BufferedWriter}
	 * @throws IOException if any element of the chain throws an {@link IOException}
	 */
	public BufferedWriter fromWriter(Charset charset) throws IOException {
		return new BufferedWriter(new OutputStreamWriter(createOutputStream(), charset));
	}
	
	/**
	 * This method creates a {@link PrintWriter} from this builder with all the chosen options. It uses the default 
	 * {@link Charset} for that.
	 * @return a {@link PrintWriter}
	 * @throws IOException if any element of the chain throws an {@link IOException}
	 */
	public PrintWriter fromPrint() throws IOException {
		return new PrintWriter(fromWriter());
	}
	
	/**
	 * This method creates a {@link PrintWriter} from this builder with all the chosen options. 
	 * @param charset the charset used to convert the characters into bytes
	 * @return a {@link PrintWriter}
	 * @throws IOException if any element of the chain throws an {@link IOException}
	 */
	public PrintWriter fromPrint(Charset charset) throws IOException {
		return new PrintWriter(fromWriter(charset));
	}
	
	/**
	 * This method creates an {@link ObjectOutputStream} from this builder with all the chosen options.
	 * @return an {@link ObjectOutputStream}
	 * @throws IOException if any element of the chain throws an {@link IOException}
	 */
	public ObjectOutputStream fromObjects() throws IOException {
		return new ObjectOutputStream(new BufferedOutputStream(createOutputStream()));
	}
	
	/**
	 * This method creates a {@link DataOutputStream} from this builder with all the chosen options.
	 * @return a {@link DataOutputStream}
	 * @throws IOException if any element of the chain throws an {@link IOException}
	 */
	public DataOutputStream fromData() throws IOException {
		return new DataOutputStream(new BufferedOutputStream(createOutputStream()));
	}
	
	/**
	 * This method writes the given Object to the output by calling {@link Objects#toString()}.
	 * @param o the object to write to the output
	 * @throws IOException if any element of the chain throws an {@link IOException}
	 */
	public void write(Object o) throws IOException {
		try(BufferedWriter out=this.fromWriter()) {
			out.write(Objects.toString(o));
		}
	}
	
	/**
	 * This method writes the given {@link Iterable} to the output by calling {@link Objects#toString()} on each
	 * of the elements and writing them on seperate lines.
	 * @param iterable the {@link Iterable} to write to the output
	 * @throws IOException if any element of the chain throws an {@link IOException}
	 */
	public void writeLines(Iterable<?> iterable) throws IOException {
		writeLines(iterable.iterator());
	}
	
	/**
	 * This method writes the given array to the output by calling {@link Objects#toString()} on each
	 * of the elements and writing them on seperate lines.
	 * @param array the array to write to the output
	 * @throws IOException if any element of the chain throws an {@link IOException}
	 */
	public <T> void writeLines(T[] array) throws IOException {
		try(BufferedWriter out=this.fromWriter()) {
			for(int i=0;i<array.length;i++) {
				if(i>0)
					out.newLine();
				out.write(Objects.toString(array[i]));
			}
		}
	}
	
	/**
	 * This method writes the given remaining content of the {@link Iterator} to the output by calling 
	 * {@link Objects#toString()} on each of the elements and writing them on seperate lines.
	 * @param iterator the {@link Iterator} to write to the output
	 * @throws IOException if any element of the chain throws an {@link IOException}
	 */
	public void writeLines(Iterator<?> iterator) throws IOException {
		try(BufferedWriter out=this.fromWriter()) {
			while(iterator.hasNext()) {
				out.write(Objects.toString(iterator.next()));
				if(iterator.hasNext())
					out.newLine();
			}
		}
	}
	
	/**
	 * This method writes the given Object to the output by calling {@link Objects#toString()}.
	 * @param o the object to write to the output
	 * @param charset the charset used to convert the characters into bytes
	 * @throws IOException if any element of the chain throws an {@link IOException}
	 */
	public void write(Object o, Charset charset) throws IOException {
		try(BufferedWriter out=this.fromWriter(charset)) {
			out.write(Objects.toString(o));
		}
	}
	
	/**
	 * This method writes the given {@link Iterable} to the output by calling {@link Objects#toString()} on each
	 * of the elements and writing them on seperate lines.
	 * @param iterable the {@link Iterable} to write to the output
	 * @param charset the charset used to convert the characters into bytes
	 * @throws IOException if any element of the chain throws an {@link IOException}
	 */
	public void writeLines(Iterable<?> iterable, Charset charset) throws IOException {
		writeLines(iterable.iterator(), charset);
	}
	
	/**
	 * This method writes the given array to the output by calling {@link Objects#toString()} on each
	 * of the elements and writing them on seperate lines.
	 * @param array the array to write to the output
	 * @param charset the charset used to convert the characters into bytes
	 * @throws IOException if any element of the chain throws an {@link IOException}
	 */
	public <T> void writeLines(T[] array, Charset charset) throws IOException {
		try(BufferedWriter out=this.fromWriter(charset)) {
			for(int i=0;i<array.length;i++) {
				if(i>0)
					out.newLine();
				out.write(Objects.toString(array[i]));
			}
		}
	}
	
	/**
	 * This method writes the given remaining content of the {@link Iterator} to the output by calling 
	 * {@link Objects#toString()} on each of the elements and writing them on seperate lines.
	 * @param iterator the {@link Iterator} to write to the output
	 * @param charset the charset used to convert the characters into bytes
	 * @throws IOException if any element of the chain throws an {@link IOException}
	 */
	public void writeLines(Iterator<?> iterator, Charset charset) throws IOException {
		try(BufferedWriter out=this.fromWriter(charset)) {
			while(iterator.hasNext()) {
				out.write(Objects.toString(iterator.next()));
				if(iterator.hasNext())
					out.newLine();
			}
		}
	}

	/**
	 * This method creates a {@link ZipOutputStream} from this builder with all the chosen options.
	 * @return a {@link ZipOutputStream}
	 * @throws IOException if any element of the chain throws an {@link IOException}
	 */
	public ZipOutputStream fromZip() throws IOException {
		return new ZipOutputStream(new BufferedOutputStream(createOutputStream()));
	}

	/**
	 * This method wraps the OutputStream created by the target object with other streams depending on what options
	 * the user chose.
	 * @return an OutputStream
	 * @throws IOException if any element of the chain throws an {@link IOException}
	 */
	@SuppressWarnings("resource")
	protected OutputStream createOutputStream() throws IOException {
		OutputStream stream=target.openStream();
		if(base64Encoder!=null)
			stream=base64Encoder.wrap(stream);
		if(compress) {
			if(target.hasName() && CompressorRegistry.getInstance().canWrapOutput(target.getName()))
				stream=CompressorRegistry.getInstance().wrap(target.getName(), stream);
			else
				stream=new DeflaterOutputStream(stream);
		}
		return stream;
	}
	
	/**
	 * @return the target this builder was created with
	 */
	public Target getTarget() {
		return target;
	}
}
