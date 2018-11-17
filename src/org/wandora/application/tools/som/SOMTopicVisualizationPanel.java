
package org.wandora.application.tools.som;


import org.wandora.utils.*;
import static org.wandora.utils.Tuples.*;
import org.wandora.topicmap.*;
import org.wandora.application.gui.*;
import java.awt.*;
import java.util.*;
import java.awt.geom.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.image.*;



/**
 *
 * @author akivela
 */
public class SOMTopicVisualizationPanel extends JPanel implements Runnable, ActionListener, MouseListener, MouseMotionListener, KeyListener {


	private static final long serialVersionUID = 1L;

	public static final String SI_PREFIX = "http://wandora.org/si/som/";
    
    public static final String ASSOCIATE_TOPICS_IN_EVERY_CELL_TO_A_CELL_SPECIFIC_GROUP = "Group topics in every cell";
    public static final String ASSOCIATE_TOPICS_IN_SELECTED_CELLS_TO_A_GROUP = "Group topics in selected cells";
    
    public static final String PERMUTATE_TOPICS_WITHIN_EVERY_CELL = "Permutate topics within every cell";
    public static final String PERMUTATE_TOPICS_WITHIN_SELECTED_CELLS = "Permutate topics within selected cells";
    public static final String PERMUTATE_ALL_TOPICS_IN_SELECTED_CELLS = "Permutate all topics in selected cells";  

    public static final String COPY_AS_IMAGE = "Copy as image";
    public static final String COPY_CELL_TOPICS_VECTORS = "Copy cell topic vectors";
    public static final String COPY_CELL_TOPICS = "Copy cell topics";
    public static final String COPY_CELL_NEURONS = "Copy cell neurons";
    
    public static final String SELECT_CELL = "Select cell";
    public static final String DESELECT_CELL = "Deselect cell";
    public static final String SELECT_ALL_CELLS = "Select all cells";
    public static final String CLEAR_SELECTION = "Clear selection";
    
    private JDialog parent = null;
    
    private int shouldStop = -1;
    private SOMMap map = null;
    private ArrayList<T3<Topic, Integer, Integer>> topicLocations = new ArrayList<T3<Topic, Integer, Integer>>();
    private ArrayList<String>[][] cellLabels = null;
    private boolean[][] selectedCells = null; 
    
    private int cellSize = 50;
    private int fontSize = 6;
    private Font cellFont = null;
    private Font defaultFont = null;
    
    private int mapSize = 1;
    private int progress = 0;
    private int progressMax = 100;

    private MouseEvent mouseEvent;
    
    private Object[] menuStruct = new Object[] {
        SELECT_CELL,
        DESELECT_CELL,
        SELECT_ALL_CELLS,
        CLEAR_SELECTION,
        "---",
        COPY_CELL_NEURONS,
        COPY_CELL_TOPICS,
        COPY_CELL_TOPICS_VECTORS,
        "---",
        COPY_AS_IMAGE,
        "---",
        ASSOCIATE_TOPICS_IN_SELECTED_CELLS_TO_A_GROUP,
        ASSOCIATE_TOPICS_IN_EVERY_CELL_TO_A_CELL_SPECIFIC_GROUP,
        "---",
        PERMUTATE_TOPICS_WITHIN_SELECTED_CELLS,
        PERMUTATE_ALL_TOPICS_IN_SELECTED_CELLS,
        PERMUTATE_TOPICS_WITHIN_EVERY_CELL,
    };
    
    private RenderingHints qualityHints = new RenderingHints(
                RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
    private RenderingHints antialiasHints = new RenderingHints(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
    private RenderingHints metricsHints = new RenderingHints(
                RenderingHints.KEY_FRACTIONALMETRICS,
                RenderingHints.VALUE_FRACTIONALMETRICS_ON);
    
    
    public SOMTopicVisualizationPanel() {
        defaultFont = new Font("SansSerif", Font.PLAIN, 12);
        this.setComponentPopupMenu(UIBox.makePopupMenu(menuStruct, this));
        this.addMouseListener(this);
        this.addMouseMotionListener(this);
        this.addKeyListener(this);
        this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }
    
    
    
    public void initialize(JDialog parent, SOMMap map) {
        cellFont = new Font("SansSerif", Font.PLAIN, fontSize);
        this.parent = parent;
        this.map = map;
        mapSize = map.getSize();
        selectedCells = new boolean[mapSize][mapSize];
        clearCellSelection();
        cellLabels = new ArrayList[mapSize][mapSize];
        if(shouldStop == -1) shouldStop = 0;
        new Thread(this).start();
    }
    
    
    public void setCellSize(int size) {
        if(cellSize != size) {
            cellSize = size;
            fontSize = Math.max(3, size / 12);
            cellFont = new Font("SansSerif", Font.PLAIN, fontSize);
            this.invalidate();
            this.repaint();
        }
    }
    
    
    
    public int getCellSize() {
        return cellSize;
    }
    
    
    public void shouldStop() {
        shouldStop = 1;
    }
    
   
    
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(cellSize*mapSize, cellSize*mapSize);
    }
    @Override
    public Dimension getMinimumSize() {
        return new Dimension(cellSize*mapSize, cellSize*mapSize);
    }
    @Override
    public Dimension getMaximumSize() {
        return new Dimension(cellSize*mapSize, cellSize*mapSize);
    }
    
    
    
    
    @Override
    public void paint(Graphics g) {
        if(g instanceof Graphics2D) ((Graphics2D) g).addRenderingHints(antialiasHints);
        
        super.paint(g);
        
        if(topicLocations!=null && map != null) {
            g.setColor(new Color(0xc8ddf2));
            for(int i=0; i<mapSize; i++) {
                for(int j=0; j<mapSize; j++) {
                    if(selectedCells[i][j]) {
                        g.fillRect(i*cellSize, j*cellSize, cellSize, cellSize);
                    }
                }   
            }
            
            g.setColor(Color.WHITE);
            for(int i=0; i<=mapSize; i++) {
                g.drawLine(i*cellSize, 0, i*cellSize, mapSize*cellSize);
                g.drawLine(0, i*cellSize, mapSize*cellSize, i*cellSize);
            }

            g.setColor(Color.BLACK);
            g.setFont(defaultFont);
            if(progress < progressMax)
                g.drawString("distributing topics "+100*progress/progressMax+"%", 10, 20);
            else
                g.drawString("ready.", 10, 20);
            
            g.setFont(cellFont);
            FontMetrics fm = g.getFontMetrics();
            Rectangle2D labelBounds = null;
            String label = null;
            Rectangle bounds = g.getClipBounds();
            if(bounds == null) bounds = new Rectangle(0,0,(1+cellSize)*mapSize,(1+cellSize)*mapSize);
            int numberOfLines = 0;
            int lineNumber = 0;
            int labelHeight = 0;
            int labelWidth = 0;
            int x = 0;
            int y = 0;
            int cellTop = 0;
            int cellLeft = 0;
            Rectangle2D cellBounds = null;
            synchronized(cellLabels) {
                ArrayList<String> labels = null;
                for(int i=0; i<mapSize; i++) {
                    for(int j=0; j<mapSize; j++) {
                        labels = cellLabels[i][j];
                        if(labels != null) {
                            cellLeft = i*cellSize;
                            cellTop = j*cellSize;
                            cellBounds = bounds.createIntersection(new Rectangle(cellLeft,cellTop, cellSize, cellSize ));
                            if(!cellBounds.isEmpty()) {
                                g.setClip(cellBounds);
                                numberOfLines = labels.size();
                                lineNumber = 0;
                                labelHeight = 0;
                                labelWidth = 0;
                                x = 0;
                                y = 0;
                                for(Iterator<String> labelIter = labels.iterator(); labelIter.hasNext(); ) {
                                    label = labelIter.next();
                                    if(label != null) {
                                        labelBounds = fm.getStringBounds(label, g);
                                        labelHeight = (int) labelBounds.getHeight();
                                        labelWidth = (int) labelBounds.getWidth();

                                        x = cellLeft+cellSize/2-(labelWidth/2);
                                        y = cellTop+cellSize/2+lineNumber*labelHeight-numberOfLines*labelHeight/2;

                                        g.drawString(label, x, y);
                                        lineNumber++;
                                    }
                                }
                                g.drawLine(cellLeft+2, (j+1)*cellSize-2, cellLeft+2+numberOfLines, (j+1)*cellSize-2);
                            }
                        }
                    }
                }
            }
            g.setClip(bounds); // Restore clip bounds to original
        }
    }
    
    
    public String getTopicLabel(Topic t) {
        try {
            return t.getBaseName();
        }
        catch(Exception e) {
            return "n.a.";
        }
    }
    
    
    
    public void run() {
        if(map != null) {
            Map<Topic,SOMVector> samples = map.getSamples();
            Set<Topic> set = samples.keySet();
            Topic t = null;
            SOMVector v = null;
            progressMax = set.size();

            for(Iterator<Topic> iter = set.iterator(); iter.hasNext() && shouldStop != 1; ) {
                try {
                    t = iter.next();
                    v = samples.get(t);
                    T3<Integer, Integer, SOMNeuron> bmu = map.getBMU(v);

                    int x = bmu.e1.intValue();
                    int y = bmu.e2.intValue();

                    synchronized(topicLocations) {
                        topicLocations.add(new T3<Topic,Integer,Integer>( t, Integer.valueOf(x), Integer.valueOf(y) ));
                    }
                    synchronized(cellLabels) {
                        if(cellLabels[x][y] == null) cellLabels[x][y] = new ArrayList<String>();
                        cellLabels[x][y].add(getTopicLabel(t));
                    }
                    progress++;
                    this.invalidate();
                    this.repaint();
                    if(parent != null) {
                        parent.invalidate();
                        parent.repaint();
                    }
                    try {
                        Thread.sleep(100);
                    }
                    catch(Exception e) { /* WAKEUP */ }
                }
                catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    // -------------------------------------------------------------------------
    
    
    
    public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
        String c = actionEvent.getActionCommand();
        boolean requiresRefresh = false;
        int x = mouseEvent.getX() / cellSize;
        int y = mouseEvent.getY() / cellSize;
            
        if(SELECT_CELL.equalsIgnoreCase(c)) {
            selectCell(x,y);
            requiresRefresh = true;
        }
        else if(DESELECT_CELL.equalsIgnoreCase(c)) {
            deselectCell(x,y);
            requiresRefresh = true;
        }
        else if(SELECT_ALL_CELLS.equalsIgnoreCase(c)) {
            selectAllCells();
            requiresRefresh = true;
        }
        else if(CLEAR_SELECTION.equalsIgnoreCase(c)) {
            clearCellSelection();
            requiresRefresh = true;
        }
        
        
        
        else if(COPY_CELL_NEURONS.equalsIgnoreCase(c)) {
            boolean noSelection = true;
            StringBuilder sb = new StringBuilder("");
            for(int i=0; i<mapSize; i++) {
                for(int j=0; j<mapSize; j++) {
                    if(selectedCells[i][j]) {
                        noSelection = false;
                        SOMNeuron n = map.getAt(i, j);
                        sb.append(i).append("\t").append(j).append("\t").append(n.toString()).append( "\n");
                    }
                }
            }
            if(noSelection) {
                if(x >= 0 && x < mapSize && y >= 0 && y < mapSize) {
                    SOMNeuron n = map.getAt(x, y);
                    sb.append(x).append("\t").append(y).append("\t").append( n.toString());
                }
            }
            ClipboardBox.setClipboard( sb.toString() );
        }
        
        
        
        else if(COPY_CELL_TOPICS_VECTORS.equalsIgnoreCase(c)) {
            StringBuffer sb = new StringBuffer("");
            boolean noSelection = true;
            for(int i=0; i<mapSize; i++) {
                for(int j=0; j<mapSize; j++) {
                    if(selectedCells[i][j]) {
                        noSelection = false;
                        getTopicVectorAsString(sb, i, j);
                    }
                }
            }
            if(noSelection) {
                if(x >= 0 && x < mapSize && y >= 0 && y < mapSize) {
                    getTopicVectorAsString(sb, x, y);
                }
            }
            String str = sb.toString();
            if(str.length() == 0) str = "no topics in cell(s)";
            ClipboardBox.setClipboard(str);
        }
        
        
        else if(COPY_CELL_TOPICS.equalsIgnoreCase(c)) {
            StringBuilder sb = new StringBuilder("");
            boolean noSelection = true;
            for(int i=0; i<mapSize; i++) {
                for(int j=0; j<mapSize; j++) {
                    if(selectedCells[i][j]) {
                        getCellTopicsAsString(sb, i, j);
                        noSelection = false;
                    }
                }
            }
            if(noSelection) {
                if(x >= 0 && x < mapSize && y >= 0 && y < mapSize) {
                    getCellTopicsAsString(sb, x ,y);
                }
            }
            String str = sb.toString();
            if(str.length() == 0) str = "no topics in cell(s)";
            ClipboardBox.setClipboard(str);
        }
        
        else if(COPY_AS_IMAGE.equalsIgnoreCase(c)) {
            BufferedImage image = new BufferedImage(cellSize*mapSize, cellSize*mapSize, BufferedImage.TYPE_INT_RGB);
            Graphics g = image.getGraphics();
            paint(g);
            ClipboardBox.setClipboard(image);
        }

        
        
        else if(ASSOCIATE_TOPICS_IN_EVERY_CELL_TO_A_CELL_SPECIFIC_GROUP.equalsIgnoreCase(c)) {
            for(int i=0; i<mapSize; i++) {
                for(int j=0; j<mapSize; j++) {
                    ArrayList<Topic> topicsInCell = topicsInCell(i, j);
                    if(topicsInCell != null && topicsInCell.size() > 1) {
                        String groupTopic = "SOM-Group-"+i+"-"+j;
                        Topic topic = null;
                        Topic[] topics = topicsInCell.toArray(new Topic[] {} );
                        int s = topics.length;
                        for(int k=0; k<s; k++) {
                            topic = topics[k];
                            groupAssociation(topic, groupTopic);
                        }
                    }
                }
            }
        }
        
        
        
        else if(ASSOCIATE_TOPICS_IN_SELECTED_CELLS_TO_A_GROUP.equalsIgnoreCase(c)) {
            boolean noSelection = true;
            String groupName = "SOM-Group-"+System.currentTimeMillis();
             
            for(int i=0; i<mapSize; i++) {
                for(int j=0; j<mapSize; j++) {
                    if(selectedCells[i][j]) {
                        noSelection = true;
                        groupTopicsInCell(groupName, i, j);
                    }
                }
            }
            if(noSelection) {
                groupTopicsInCell(groupName, x, y);
            }
        }
        
        
        else if(PERMUTATE_TOPICS_WITHIN_SELECTED_CELLS.equalsIgnoreCase(c)) {
            boolean noSelection = true;
            for(int i=0; i<mapSize; i++) {
                for(int j=0; j<mapSize; j++) {
                    if(selectedCells[i][j]) {
                        noSelection = false;
                        associateTopicsTogetherInCell(i, j);
                    }
                }
            }
            if(noSelection) {
                associateTopicsTogetherInCell(x, y);
            }
        }
        
        
        else if(PERMUTATE_ALL_TOPICS_IN_SELECTED_CELLS.equalsIgnoreCase(c)) {
            ArrayList<Topic> selectedTopics = topicsInCellSelection();
            if(selectedTopics != null && selectedTopics.size() > 1) {
                Topic player1 = null;
                Topic player2 = null;
                Topic[] topics = selectedTopics.toArray(new Topic[] {} );
                int s = topics.length;
                for(int i=0; i<s; i++) {
                    for(int j=0; j<s; j++) {
                        if(i==j) continue;
                        player1 = topics[i];
                        player2 = topics[j];
                        similarityAssociation(player1, player2);
                    }
                }
            }
        }

        
        else if(PERMUTATE_TOPICS_WITHIN_EVERY_CELL.equalsIgnoreCase(c)) {
            for(int x2=0; x2<mapSize; x2++) {
                for(int y2=0; y2<mapSize; y2++) {
                    ArrayList<Topic> topicsInCell = topicsInCell(x2, y2);
                    if(topicsInCell != null && topicsInCell.size() > 1) {
                        Topic player1 = null;
                        Topic player2 = null;
                        Topic[] topics = topicsInCell.toArray(new Topic[] {} );
                        int s = topics.length;
                        for(int i=0; i<s; i++) {
                            for(int j=0; j<s; j++) {
                                if(i==j) continue;
                                player1 = topics[i];
                                player2 = topics[j];
                                strongSimilarityAssociation(player1, player2);
                            }
                        }
                    }
                }
            }
        }

        else {
            // Should not be here!
        }
        
        
        if(requiresRefresh) {
            if(parent != null) {
                parent.invalidate();
                parent.repaint();
            }
        }
    }
    
    
    
    
    
    public void associateTopicsTogetherInCell(int x, int y) {
        ArrayList<Topic> topicsInCell = topicsInCell(x, y);
        if(topicsInCell != null && topicsInCell.size() > 1) {
            Topic player1 = null;
            Topic player2 = null;
            Topic[] topics = topicsInCell.toArray(new Topic[] {} );
            int s = topics.length;
            for(int i=0; i<s; i++) {
                for(int j=0; j<s; j++) {
                    if(i==j) continue;
                    player1 = topics[i];
                    player2 = topics[j];
                    strongSimilarityAssociation(player1, player2);
                }
            }
        }
    }
    
    
    
    public void groupTopicsInCell(String groupName, int x, int y) {
        ArrayList<Topic> topicsInCell = topicsInCell(x, y);
        if(topicsInCell != null && topicsInCell.size() > 1) {
            Topic topic = null;
            Topic[] topics = topicsInCell.toArray(new Topic[] {} );
            int s = topics.length;
            for(int k=0; k<s; k++) {
                topic = topics[k];
                groupAssociation(topic, groupName);
            }
        }
    }
    
    
    
    
    public void getCellTopicsAsString(StringBuilder sb, int x, int y) {
        synchronized(cellLabels) {
            ArrayList<String> labels = cellLabels[x][y];
            sb.append(x+"\t");
            sb.append(y+"\t");
            if(labels != null && labels.size() > 0) {
                String label;
                for(Iterator<String> labelIter = labels.iterator(); labelIter.hasNext(); ) {
                    label = labelIter.next();
                    if(label != null) {
                        sb.append(label+"\t");
                    }
                }
            }
        }
        sb.append("\n");
    }
    
    
    
    public void getTopicVectorAsString(StringBuffer sb, int x, int y) {
        SOMVector v = null;
        T3<Topic, Integer, Integer> location = null;
        synchronized(topicLocations) {
            for(Iterator<T3<Topic, Integer, Integer>> locations = topicLocations.iterator(); locations.hasNext(); ) {
                location = locations.next();
                if(x == location.e2.intValue() && y == location.e3.intValue()) {
                    v = map.getSampleFor(location.e1);
                    if(v != null) {
                        sb.append(x).append("\t");
                        sb.append(y).append("\t");
                        sb.append(v.toString()).append("\n");
                    }
                }
            }
        }
    }
    
    
    public ArrayList<Topic> getTopicsInNearCells(int x, int y) {
        ArrayList<Topic> nearTopicsArray = new ArrayList<Topic>();
        nearTopicsArray.addAll(topicsInCell(x-1, y-1));
        nearTopicsArray.addAll(topicsInCell(x-1, y));
        nearTopicsArray.addAll(topicsInCell(x-1, y+1));
        nearTopicsArray.addAll(topicsInCell(x, y-1));
        nearTopicsArray.addAll(topicsInCell(x, y+1));
        nearTopicsArray.addAll(topicsInCell(x+1, y-1));
        nearTopicsArray.addAll(topicsInCell(x+1, y));
        nearTopicsArray.addAll(topicsInCell(x+1, y+1));
        return nearTopicsArray;
    }
    
    
    
    public ArrayList<Topic> topicsInCellSelection() {
        ArrayList<Topic> topicsInSelection = new ArrayList<Topic>();
        for(int i=0; i<mapSize; i++) {
            for(int j=0; j<mapSize; j++) {
                if(selectedCells[i][j]) {
                    topicsInSelection.addAll(topicsInCell(i, j));
                }
            }
        }
        return topicsInSelection;
    }
    
    
    
    public ArrayList<Topic> topicsInCell(int x, int y) {
        ArrayList<Topic> topicsInCell = new ArrayList<Topic>();
        if(x >= 0 && x < mapSize && y >= 0 && y < mapSize) {
            T3<Topic, Integer, Integer> location = null;
            synchronized(topicLocations) {
                for(Iterator<T3<Topic, Integer, Integer>> locations = topicLocations.iterator(); locations.hasNext(); ) {
                    location = locations.next();
                    if(x == location.e2.intValue() && y == location.e3.intValue()) {
                        topicsInCell.add(location.e1);
                    }
                }
            }
        }
        return topicsInCell;
    }
    
    
    
    public Topic getOrCreateTopic(TopicMap tm, String basename) throws TopicMapException {
        Topic t = tm.getTopicWithBaseName(basename);
        if(t == null) {
            t = tm.createTopic();
            t.addSubjectIdentifier(new Locator(SI_PREFIX+basename));
            t.setBaseName(basename);
        }
        return t;
    }
    
    
    
    
    public void strongSimilarityAssociation(Topic player1, Topic player2) {
        try {
            TopicMap tm = player1.getTopicMap();
            Topic atype = getOrCreateTopic(tm, "Strong-SOM-Similarity");
            Topic role1 = getOrCreateTopic(tm, "Strong-SOM-Similarity-Role1");
            Topic role2 = getOrCreateTopic(tm, "Strong-SOM-Similarity-Role2");
            Association a = tm.createAssociation(atype);
            a.addPlayer(player1, role1);
            a.addPlayer(player2, role2);
        }
        catch(Exception e) {
            WandoraOptionPane.showMessageDialog(this, e.toString(), e.toString(), WandoraOptionPane.ERROR_MESSAGE);
        }
    }
    
    
    public void similarityAssociation(Topic player1, Topic player2) {
        try {
            TopicMap tm = player1.getTopicMap();
            Topic atype = getOrCreateTopic(tm, "SOM-Similarity");
            Topic role1 = getOrCreateTopic(tm, "SOM-Similarity-Role1");
            Topic role2 = getOrCreateTopic(tm, "SOM-Similarity-Role2");
            Association a = tm.createAssociation(atype);
            a.addPlayer(player1, role1);
            a.addPlayer(player2, role2);
        }
        catch(Exception e) {
            WandoraOptionPane.showMessageDialog(this, e.toString(), e.toString(), WandoraOptionPane.ERROR_MESSAGE);
        }
    }
    
    
    public void nearSimilarityAssociation(Topic player1, Topic player2) {
        try {
            TopicMap tm = player1.getTopicMap();
            Topic atype = getOrCreateTopic(tm, "SOM-near");
            Topic role1 = getOrCreateTopic(tm, "SOM-cell-topic");
            Topic role2 = getOrCreateTopic(tm, "SOM-near-cell-topic");
            Association a = tm.createAssociation(atype);
            a.addPlayer(player1, role1);
            a.addPlayer(player2, role2);
        }
        catch(Exception e) {
            WandoraOptionPane.showMessageDialog(this, e.toString(), e.toString(), WandoraOptionPane.ERROR_MESSAGE);
        }
    }
    
    
    public void groupAssociation(Topic topic, String groupTopicName) {
        try {
            TopicMap tm = topic.getTopicMap();
            Topic atype = getOrCreateTopic(tm, "SOM-Group");
            Topic role1 = getOrCreateTopic(tm, "SOM-Topic");
            Topic role2 = getOrCreateTopic(tm, "SOM-Group");
            Topic groupTopic = getOrCreateTopic(tm, groupTopicName);
            groupTopic.addType(atype);
            Association a = tm.createAssociation(atype);
            a.addPlayer(topic, role1);
            a.addPlayer(groupTopic, role2);
        }
        catch(Exception e) {
            WandoraOptionPane.showMessageDialog(this, e.toString(), e.toString(), WandoraOptionPane.ERROR_MESSAGE);
        }
    }
    
    
    
    // -------------------------------------------------------------------------
    
    public void selectCell(int x, int y) {
        if(x >= 0 && x < mapSize && y >= 0 && y < mapSize) {
            selectedCells[x][y] = true;
        }
    }
    
    
    public void deselectCell(int x, int y) {
        if(x >= 0 && x < mapSize && y >= 0 && y < mapSize) {
            selectedCells[x][y] = false;
        }
    }
    
    public void toggleCellSelection(int x, int y) {
        if(x >= 0 && x < mapSize && y >= 0 && y < mapSize) {
            selectedCells[x][y] = !selectedCells[x][y];
        }
    }
    
    
    public void clearCellSelection() {
        for(int i=0; i<mapSize; i++) {
            for(int j=0; j<mapSize; j++) {
                selectedCells[i][j] = false;
            }
        }
    }
    
    public void selectAllCells() {
        for(int i=0; i<mapSize; i++) {
            for(int j=0; j<mapSize; j++) {
                selectedCells[i][j] = true;
            }
        }
    }
    
    public boolean getCellState(int x, int y) {
        if(x >= 0 && x < mapSize && y >= 0 && y < mapSize) {
            return selectedCells[x][y];
        }
        return false;
    }
    
    // -------------------------------------------------------------------------
    
    
    
        
    public void mouseClicked(java.awt.event.MouseEvent mouseEvent) {
        this.mouseEvent = mouseEvent;
    }
    
    
    
    public void mouseEntered(java.awt.event.MouseEvent mouseEvent) {

    }
    
    public void mouseExited(java.awt.event.MouseEvent mouseEvent) {
    }
    
    public void mousePressed(java.awt.event.MouseEvent mouseEvent) {
        this.requestFocus();
        this.mouseEvent = mouseEvent;
        if(mouseEvent.getButton() == MouseEvent.BUTTON1) {
            int x = mouseEvent.getX() / cellSize;
            int y = mouseEvent.getY() / cellSize;

            if(mouseEvent.isShiftDown()) {
                toggleCellSelection(x, y);
            }
            else if(mouseEvent.isAltDown()) {
                deselectCell(x, y);
            }
            else {
                clearCellSelection();
                selectCell(x, y);
            }

            if(parent != null) {
                parent.invalidate();
                parent.repaint();
            }
        }
    }
    
    public void mouseReleased(java.awt.event.MouseEvent mouseEvent) {
        this.mouseEvent = mouseEvent;
    }
    
    
    public void mouseDragged(MouseEvent mouseEvent) {
        boolean requiresRefresh = false;
        int x = mouseEvent.getX() / cellSize;
        int y = mouseEvent.getY() / cellSize;
        if(mouseEvent.isShiftDown()) {
            if(!getCellState(x, y)) {
                selectCell(x, y);
                requiresRefresh = true;
            }
        }
        else if(mouseEvent.isAltDown()) {
            if(getCellState(x, y)) {
                deselectCell(x, y);
                requiresRefresh = true;
            }
        }
        else {
            if(!getCellState(x, y)) {
                clearCellSelection();
                selectCell(x, y);
                requiresRefresh=true;
            }
        }
        if(requiresRefresh) {
            if(parent != null) {
                parent.invalidate();
                parent.repaint();
            }
        }
    }
    
    
    
    public void mouseMoved(MouseEvent mouseEvent) {  

    }
    
    
    
    
    // -------------------------------------------------------- KEY LISTENER ---
    
    
    
    public void keyPressed(KeyEvent e) {
        boolean requiresRefresh = false;
        if(e.isControlDown()) {
            
            // --- SELECT ALL
            if(e.getKeyCode() == KeyEvent.VK_A) {
                this.selectAllCells();
                requiresRefresh = true;
            }
            
            // --- COPY TOPICS
            else if(e.getKeyCode() == KeyEvent.VK_C) {
                StringBuilder sb = new StringBuilder("");
                for(int i=0; i<mapSize; i++) {
                    for(int j=0; j<mapSize; j++) {
                        if(selectedCells[i][j]) {
                            getCellTopicsAsString(sb, i, j);
                        }
                    }
                }
                String str = sb.toString();
                if(str.length() == 0) str = "no topics in cell(s)";
                ClipboardBox.setClipboard(str);
            }
            
            // --- CREATE GROUP ASSOCIATIONS
            else if(e.getKeyCode() == KeyEvent.VK_G) {
                String groupName = "SOM-Group-"+System.currentTimeMillis();
                for(int i=0; i<mapSize; i++) {
                    for(int j=0; j<mapSize; j++) {
                        if(selectedCells[i][j]) {
                            groupTopicsInCell(groupName, i, j);
                        }
                    }
                }
            }
        }
        else if(e.isShiftDown()) {
            
            // --- EXTEND SELECTION UP
            if(e.getKeyCode() == KeyEvent.VK_UP) {
                for(int i=0; i<mapSize; i++) {
                    for(int j=1; j<mapSize; j++) {
                        if(selectedCells[i][j]) {
                            selectedCells[i][j-1] = true;
                            requiresRefresh = true;
                        }
                    }
                }
            }
            
            // --- EXTEND SELECTION DOWN
            else if(e.getKeyCode() == KeyEvent.VK_DOWN) {
                for(int i=0; i<mapSize; i++) {
                    for(int j=mapSize-2; j>=0; j--) {
                        if(selectedCells[i][j]) {
                            selectedCells[i][j+1] = true;
                            requiresRefresh = true;
                        }
                    }
                }
            }
            
            // --- EXTEND SELECTION LEFT
            else if(e.getKeyCode() == KeyEvent.VK_LEFT) {
                for(int i=1; i<mapSize; i++) {
                    for(int j=0; j<mapSize; j++) {
                        if(selectedCells[i][j]) {
                            selectedCells[i-1][j] = true;
                            requiresRefresh = true;
                        }
                    }
                }
            }
            
            // --- EXTEND SELECTION RIGHT
            else if(e.getKeyCode() == KeyEvent.VK_RIGHT) {
                for(int i=mapSize-2; i>=0; i--) {
                    for(int j=0; j<mapSize; j++) {
                        if(selectedCells[i][j]) {
                            selectedCells[i+1][j] = true;
                            requiresRefresh = true;
                        }
                    }
                }
            }
        }
        else {
            
            
            // --- MOVE SELECTION UP
            if(e.getKeyCode() == KeyEvent.VK_UP) {
                for(int i=0; i<mapSize; i++) {
                    selectedCells[i][0] = false;
                }
                for(int i=0; i<mapSize; i++) {
                    for(int j=1; j<mapSize; j++) {
                        if(selectedCells[i][j]) {
                            selectedCells[i][j] = false;
                            selectedCells[i][j-1] = true;
                            requiresRefresh = true;
                        }
                    }
                }
            }
            
            // --- MOVE SELECTION DOWN
            else if(e.getKeyCode() == KeyEvent.VK_DOWN) {
                for(int i=0; i<mapSize; i++) {
                    selectedCells[i][mapSize-1] = false;
                }
                for(int i=0; i<mapSize; i++) {
                    for(int j=mapSize-2; j>=0; j--) {
                        if(selectedCells[i][j]) {
                            selectedCells[i][j] = false;
                            selectedCells[i][j+1] = true;
                            requiresRefresh = true;
                        }
                    }
                }
            }
            
            // --- MOVE SELECTION LEFT
            else if(e.getKeyCode() == KeyEvent.VK_LEFT) {
                for(int j=0; j<mapSize; j++) {
                    selectedCells[0][j] = false;
                }
                for(int i=1; i<mapSize; i++) {
                    for(int j=0; j<mapSize; j++) {
                        if(selectedCells[i][j]) {
                            selectedCells[i][j] = false;
                            selectedCells[i-1][j] = true;
                            requiresRefresh = true;
                        }
                    }
                }
            }
            
            // --- MOVE SELECTION RIGHT
            else if(e.getKeyCode() == KeyEvent.VK_RIGHT) {
                for(int j=0; j<mapSize; j++) {
                    selectedCells[mapSize-1][j] = false;
                }
                for(int i=mapSize-2; i>=0; i--) {
                    for(int j=0; j<mapSize; j++) {
                        if(selectedCells[i][j]) {
                            selectedCells[i][j] = false;
                            selectedCells[i+1][j] = true;
                            requiresRefresh = true;
                        }
                    }
                }
            }
        }
        if(requiresRefresh) {
            if(parent != null) {
                parent.invalidate();
                parent.repaint();
            }
        }
    }
    
    public void keyReleased(KeyEvent e) {
        
    }
    
    public void keyTyped(KeyEvent e) {
        
    }
    
    
}
