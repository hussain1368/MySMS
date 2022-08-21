package com.kabulbits.shoqa.util;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

public abstract class BgWorker<E> implements ActionListener 
{
	private Component comp;
	private SwingWorker<E, Void> worker;
	
	public BgWorker(){
		this(null);
	}
	public BgWorker(Component comp){
		this.comp = comp;
	}
	@Override
	public void actionPerformed(ActionEvent e){
		worker = new SwingWorker<E, Void>(){
			@Override
			protected E doInBackground() throws Exception {
//				Thread.sleep(1000);
				return save();
			}
			@Override
			protected void done(){
				if(comp != null){
					comp.setCursor(Cursor.getDefaultCursor());
				}
				try{
					finish(get());
				}
				catch(InterruptedException e){
					if(App.LOG){
						App.getLogger().error(e.getMessage(), e);
					}
				}
				catch(ExecutionException e){
					if(App.LOG){
						App.getLogger().error(e.getMessage(), e);
					}
				}
			}
		};
		if(comp != null){
			comp.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		}
		worker.execute();
	}
	protected abstract E save();
	protected abstract void finish(E result);
}
