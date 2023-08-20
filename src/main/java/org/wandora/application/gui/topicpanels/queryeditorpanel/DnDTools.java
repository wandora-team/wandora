/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2023 Wandora Team
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.wandora.application.gui.topicpanels.queryeditorpanel;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

import org.wandora.query2.DirectiveUIHints;

/**
 *
 * @author olli
 */


public class DnDTools {
    public static class ChainedTransferHandler extends TransferHandler {

		private static final long serialVersionUID = 1L;
		
		protected ChainedTransferHandler next;
        public ChainedTransferHandler(){
        }
        public ChainedTransferHandler(ChainedTransferHandler next){
            this.next=next;
        }
        public ChainedTransferHandler(ChainedTransferHandler next,String property){
            super(property);
            this.next=next;
        }

        public boolean chainCanImport(TransferHandler.TransferSupport support){return false;}
        
        @Override
        public boolean canImport(TransferHandler.TransferSupport support) {
            if(chainCanImport(support)) return true;
            else if(next!=null) return next.canImport(support);
            else return super.canImport(support);
        }

        public boolean chainImportData(TransferHandler.TransferSupport support){return false;}
        
        @Override
        public boolean importData(TransferHandler.TransferSupport support) {
            if(chainImportData(support)) return true;
            else if(next!=null) return next.importData(support);
            else return super.importData(support);
        }
        
        @Override
        protected Transferable createTransferable(JComponent c) {
            if(next!=null) return next.createTransferable(c);
            else return super.createTransferable(c);
        }            
        @Override
        public int getSourceActions(JComponent c) {
            if(next!=null) return next.getSourceActions(c);
            else return super.getSourceActions(c);
        }        
    }
    
    
    public static <K> void addDropTargetHandler(final JComponent component,final DataFlavor flavor,final DropTargetCallback<K> callback){
        
        final TransferHandler old=component.getTransferHandler();
        if(old!=null && !(old instanceof ChainedTransferHandler)){
            throw new RuntimeException("Tried to chain transfer handlers but existing handler is not a chained version.");
        }
        
        component.setTransferHandler(new ChainedTransferHandler((ChainedTransferHandler)old){
            @Override
            public boolean chainCanImport(TransferHandler.TransferSupport support) {
                return support.isDataFlavorSupported(flavor);
            }

            @Override
            public boolean chainImportData(TransferHandler.TransferSupport support) {
                if(!chainCanImport(support)) return false;
                
                Transferable t=support.getTransferable();
                try{
                    Object data=t.getTransferData(flavor);
                    K cast=(K)data;
                    return callback.callback(component, cast, support);
                }
                catch(UnsupportedFlavorException | IOException e){return false;}
            }
        });
    }
    
    public static <K> void setDragSourceHandler(final JComponent component,final String property, final WrapperDataFlavor<K> dataFlavor,final DragSourceCallback<K> callback){

        final TransferHandler old=component.getTransferHandler();
        if(old!=null && !(old instanceof ChainedTransferHandler)){
            throw new RuntimeException("Tried to chain transfer handlers but existing handler is not a chained version.");
        }
        
        component.setTransferHandler(new ChainedTransferHandler((ChainedTransferHandler)old,property){
            @Override
            protected Transferable createTransferable(JComponent c) {
                K wrapped=callback.callback(component);
                if(wrapped!=null) return makeTransferable(dataFlavor, wrapped);
                else return super.createTransferable(c);
            }

            @Override
            public int getSourceActions(JComponent c) {
                return TransferHandler.COPY|super.getSourceActions(c);
            }
        });
        component.addMouseListener(new MouseAdapter(){
            @Override
            public void mousePressed(MouseEvent e) {
                JComponent comp=(JComponent)e.getSource();
                TransferHandler th = comp.getTransferHandler();
                th.exportAsDrag(comp, e, TransferHandler.COPY);
            }
        });
        
    }
    
    public static interface DropTargetCallback<K>{
        public boolean callback(JComponent component,K o, TransferHandler.TransferSupport support);
    }
    
    public static interface DragSourceCallback<K>{
        public K callback(JComponent component);
    }
    
    public static class WrapperDataFlavor<K> extends DataFlavor {
        private final Class<K> cls;
        public WrapperDataFlavor(String mimeType,Class<K> cls) throws ClassNotFoundException {
            super(mimeType);
            this.cls=cls;
        }
        public Class<K> getDataClass(){return cls;}
    }
    
    public static <K> WrapperDataFlavor makeDataFlavor(Class<K> cls){
        try{
            return new WrapperDataFlavor<>(DataFlavor.javaJVMLocalObjectMimeType+";class="+cls.getName(),cls);
        }catch(ClassNotFoundException cnfe){
            throw new RuntimeException(cnfe);
        }
    }
    
    public static final WrapperDataFlavor<DirectiveUIHints> directiveHintsDataFlavor=makeDataFlavor(DirectiveUIHints.class);
    public static final WrapperDataFlavor<DirectivePanel> directivePanelDataFlavor=makeDataFlavor(DirectivePanel.class);

    public static class WrapperTransferable<K> implements Transferable {
        private final K wrapped;
        private final WrapperDataFlavor<K> dataFlavor;
        public WrapperTransferable(WrapperDataFlavor<K> dataFlavor,K wrapped){
            this.dataFlavor=dataFlavor;
            this.wrapped=wrapped;
        }
        
        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[]{dataFlavor};
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return flavor.equals(dataFlavor);
        }

        @Override
        public K getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            if(flavor.equals(dataFlavor)) return wrapped;
            else throw new UnsupportedFlavorException(flavor);
        }
        
    }
    
    public static <K> WrapperTransferable<K> makeTransferable(WrapperDataFlavor<K> dataFlavor,K wrapped){
        return new WrapperTransferable<>(dataFlavor,wrapped);
    }
    
    public static WrapperTransferable<DirectiveUIHints> makeDirectiveHintsTransferable(DirectiveUIHints hints){
        return makeTransferable(directiveHintsDataFlavor,hints);
    }
    
    public static WrapperTransferable<DirectivePanel> makeDirectivePanelTransferable(DirectivePanel directivePanel){
        return makeTransferable(directivePanelDataFlavor,directivePanel);
    }

}
