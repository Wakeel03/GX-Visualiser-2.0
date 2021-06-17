package application;

import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import javax.swing.SwingUtilities;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.stage.Screen;
import javafx.stage.Stage;

import javafx.scene.*;

public class Menu {
	private Stage window;
	
	public static  int WIDTH;
	public static int HEIGHT;
	
	private GridPane grid;
	
	private ScalableCoordinateSystem sc;
	private Pane graph;
	
	private String selected_tool = "";
	
	private String selected_line_algo = "DDA";
	private String selected_line_option = "Solid";
	
	private Color color;
	private Color polygon_stroke_color = Color.BLACK;
	private Color circle_color = Color.BLACK;
	
	private double mouseDragStartX, mouseDragStartY;
	
	CheckBox cb_no_fill;
	
	private Spinner<Integer> num_sides_picker;
	private Spinner<Integer> sides_picker;
	private Spinner<Integer> stroke_picker;
	private Spinner<Integer> size_picker;
	private Spinner<Integer> radius_picker;
	private Spinner<Integer> circle_stroke_picker;
	private Spinner<Integer> thickness_picker;
	
	private Spinner<Integer> translateX_picker;
	private Spinner<Integer> translateY_picker;
	private Spinner<Integer> scaleFactorX_picker;
	private Spinner<Integer> scaleFactorY_picker;
	private Spinner<Integer> rotateAngle_picker;
	private Spinner<Integer> fixedPointX_picker;
	private Spinner<Integer> fixedPointY_picker;
	private Spinner<Integer> shearValue_picker;
	
	private RadioButton rb1;
	private RadioButton rb2;
	
	private RadioButton rb_reflection_X;
	private RadioButton rb_reflection_Y;
	private RadioButton rb_reflection_XY;
	private RadioButton rb_reflection_YX;
	
	private ArrayList<Double> clicked_points;
	private ArrayList<Double> current_points_to_transform;
	
	private String type = "Regular";
	private String transformation_type = "";
	
	private boolean isClipping = false;
	
	public Menu(Stage window) {
		this.window = window;
		clicked_points = new ArrayList<Double>();
		current_points_to_transform = new ArrayList<Double>();
		
		Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
		WIDTH = (int)(primaryScreenBounds.getWidth());
		HEIGHT = (int)(primaryScreenBounds.getHeight());
		
		/*Rectangle gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
		WIDTH = (int) gd.getWidth();
		HEIGHT = (int)gd.getHeight();*/
	}

	public Scene getMenuScene() {
		ScrollPane s1 = new ScrollPane();
		s1.setMaxHeight(HEIGHT);
		s1.setMinHeight(HEIGHT);
		s1.setPrefHeight(HEIGHT);
		
		s1.setMaxWidth(380);
		s1.setMinWidth(380);
		s1.setPrefWidth(380);
		
		VBox sidebar = createSidebar();

		sidebar.setMinHeight(HEIGHT);
		
		s1.setContent(sidebar);
		
		sc = new ScalableCoordinateSystem(this);
		
		grid = new GridPane();
		grid.setHgap(100);
		
		GridPane.setConstraints(s1, 0, 0);
		
		grid.getChildren().add(s1);
		
		drawGraph();
		
		//grid.setAlignment(Pos.CENTER_LEFT);
		Scene scene = new Scene(grid, WIDTH, HEIGHT);
		scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
		
		return scene;
	}
		
	public void drawGraph() {
		if (isClipping) {
			drawClippingWindow();
		}
		else {
			grid.getChildren().remove(graph);
	        graph = sc.drawGraph();
	        addMouseScrolling(graph);
	        addMouseClickListener(graph);
	        GridPane.setConstraints(graph, 1, 0);
	        grid.getChildren().add(graph);			
		}

	}
	
	public void addMouseScrolling(Node node) {
        node.setOnScroll((ScrollEvent event) -> {
            double deltaY = event.getDeltaY();
            sc.zoom(deltaY);
            drawGraph();
        });
    }
	
	public void addMouseClickListener(Node node) {
		node.setOnMouseClicked(e -> {
			if (selected_tool.equals("") && !isClipping) return;
			double[] mouseCoordinates = sc.getMouseCoordinates(e.getX(), e.getY());
			//System.out.println(mouseCoordinates[0] + ", " + mouseCoordinates[1]);
			
			if (selected_tool.equals("Line Algorithm") || isClipping) {
				clicked_points.add(mouseCoordinates[0]);
				clicked_points.add(mouseCoordinates[1]);
				
				//DrawPolygon circle_point = new DrawPolygon(0, 0, color, "circle_point", 0, mouseCoordinates[0], mouseCoordinates[1], clicked_points);
				sc.addPointCircle(new double[] {mouseCoordinates[0], mouseCoordinates[1]});
				
				if (clicked_points.size() == 4) {
					if (isClipping)
						sc.addBresenhamPath(addLine(), selected_line_option.equals("Dashed"), selected_line_option.equals("Dotted"), 1);
					else if (selected_line_algo.equals("Bresenham Algorithm"))
						sc.addBresenhamPath(addLine(), selected_line_option.equals("Dashed"), selected_line_option.equals("Dotted"), thickness_picker.getValue());
					else if (selected_line_algo.equals("DDA"))
						sc.addDDAPath(addLine(), selected_line_option.equals("Dashed"), selected_line_option.equals("Dotted"), thickness_picker.getValue());
					
					clicked_points.clear();
				}
			}
			
			else if (selected_tool.equals("Polygon Drawing")) {
				if (type.equals("Irregular")) {
					clicked_points.add(mouseCoordinates[0]);
					clicked_points.add(mouseCoordinates[1]);
					sc.addPointCircle(new double[] {mouseCoordinates[0], mouseCoordinates[1]});
				}
				
				if (type.equals("Regular") || clicked_points.size() / 2 == num_sides_picker.getValue()) {
					System.out.println(num_sides_picker.getValue());
					DrawPolygon polygon = new DrawPolygon(num_sides_picker.getValue(), stroke_picker.getValue(), cb_no_fill.isSelected() ? null : color, type, size_picker.getValue(), polygon_stroke_color, mouseCoordinates[0], mouseCoordinates[1], clicked_points);
					sc.addShape(polygon);
				}
							
				if (clicked_points.size() / 2 == num_sides_picker.getValue()) clicked_points.clear();
			}
			else if (selected_tool.equals("Transformations")) {
				if (transformation_type.equals("")) {
					Alert a = new Alert(AlertType.NONE);
					 a.setAlertType(AlertType.ERROR);
	                a.setContentText("Please choose a type of transformation to proceed");
	                // show the dialog
	                a.show();
	                
	                return;
				}
				
				if (clicked_points.isEmpty()) current_points_to_transform.clear();
				
				clicked_points.add(mouseCoordinates[0]);
				clicked_points.add(mouseCoordinates[1]);
				sc.addPointCircle(new double[] {mouseCoordinates[0], mouseCoordinates[1]});

				
				if (clicked_points.size() / 2 == sides_picker.getValue()) {
					DrawPolygon polygon = new DrawPolygon(sides_picker.getValue(), stroke_picker.getValue(), null, "Irregular", size_picker.getValue(), polygon_stroke_color, mouseCoordinates[0], mouseCoordinates[1], clicked_points);
					sc.addShape(polygon);
				}
					
				if (clicked_points.size() / 2 == sides_picker.getValue()) {
					for (int i = 0; i < clicked_points.size(); i++) {
						current_points_to_transform.add(clicked_points.get(i));
					}
					clicked_points.clear();
				}
			}
			else {
				sc.addCircle(radius_picker.getValue(), circle_stroke_picker.getValue(), circle_color, mouseCoordinates[0], mouseCoordinates[1]);
				
			}
			
			drawGraph();
			
		});
	}
	
	private VBox createSidebar() {
		VBox sidebar = new VBox(40);
		//sidebar.prefWidthProperty().bind(window.widthProperty().multiply(0.2));
		//sidebar.setPrefWidth(WIDTH / 4);
		sidebar.getStyleClass().add("sidebar");
		//sidebar.setBackground(new Background(new BackgroundFill(Color.rgb(255, 0, 0), null, null)));
		
		Label lbl_title = new Label("GX VISUALISER");
		lbl_title.getStyleClass().add("sidebar__title");
		sidebar.getChildren().add(lbl_title);
		
		VBox tools = new VBox(20);
		
		Label lbl_tools = new Label("TOOLS");
		lbl_tools.getStyleClass().add("sidebar__subtitle");
		
		HBox tool_line = new HBox();
		Label lbl_line = new Label("Line");
		lbl_line.getStyleClass().add("sidebar__tool");
		Button btn_line = new Button(">");
		
		btn_line.getStyleClass().add("sidebar__btn");
		tool_line.getChildren().addAll(lbl_line, btn_line);
				
		HBox tool_polygon = new HBox();
		Label lbl_polygon = new Label("Polygon");
		lbl_polygon.getStyleClass().add("sidebar__tool");
		Button btn_polygon = new Button(">");
	
		btn_polygon.getStyleClass().add("sidebar__btn");
		tool_polygon.getChildren().addAll(lbl_polygon, btn_polygon);
		
		HBox tool_circle = new HBox();
		Label lbl_circle = new Label("Circle");
		lbl_circle.getStyleClass().add("sidebar__tool");
		Button btn_circle = new Button(">");
		
		btn_circle.getStyleClass().add("sidebar__btn");
		tool_circle.getChildren().addAll(lbl_circle, btn_circle);
		
		HBox tool_transformations = new HBox();
		Label lbl_tranformations = new Label("Transformations");
		lbl_tranformations.getStyleClass().add("sidebar__tool");
		Button btn_transformations = new Button(">");
		
		btn_transformations.getStyleClass().add("sidebar__btn");
		tool_transformations.getChildren().addAll(lbl_tranformations, btn_transformations);

		
		VBox line_algo_tools = lineAlgoTools();
		VBox draw_polygons_tools = drawPolygonsTools();
		VBox circle_algo_tools= circleAlgoTools();
		VBox transformations_tools = transformationsTools();
		
		Button btn_line_clip = new Button("Cohen-Sutherland Line Clipping");
		btn_line_clip.getStyleClass().add("sidebar__btnLarge_fullCurve");
		btn_line_clip.setPrefSize(200, 40);
		
		btn_line_clip.setOnAction(e -> {
			lineClipping();
		});
		
		btn_line.setOnAction(e -> {
			if (selected_tool.equals("Line Algorithm")) {
				tools.getChildren().removeAll(line_algo_tools, tool_polygon, tool_circle, tool_transformations, btn_line_clip);
				tools.getChildren().addAll(tool_polygon, tool_circle, tool_transformations, btn_line_clip);
				selected_tool = "";
			}else {
				selected_tool = "Line Algorithm";
				isClipping = false;
				drawGraph();
				tools.getChildren().removeAll(circle_algo_tools, draw_polygons_tools, tool_polygon, tool_circle, tool_transformations, transformations_tools, btn_line_clip);
				tools.getChildren().addAll(line_algo_tools, tool_polygon, tool_circle, tool_transformations, btn_line_clip);
			}
		});
		
		btn_polygon.setOnAction(e -> {
			if (selected_tool.equals("Polygon Drawing")) {
				tools.getChildren().removeAll(draw_polygons_tools, tool_circle, tool_transformations, btn_line_clip);
				tools.getChildren().addAll(tool_circle, tool_transformations, btn_line_clip);
				selected_tool = "";
			}else {
				selected_tool = "Polygon Drawing";
				isClipping = false;
				drawGraph();
				tools.getChildren().removeAll(line_algo_tools, circle_algo_tools, tool_circle, tool_transformations, transformations_tools, btn_line_clip);
				tools.getChildren().addAll(draw_polygons_tools, tool_circle, tool_transformations, btn_line_clip);
			}
		});
		
		btn_circle.setOnAction(e -> {
			if (selected_tool.equals("Circle Drawing")) {
				tools.getChildren().removeAll(circle_algo_tools, tool_transformations, btn_line_clip);
				tools.getChildren().addAll(tool_transformations, btn_line_clip);
				selected_tool = "";
			}else {
				selected_tool = "Circle Drawing";
				isClipping = false;
				drawGraph();
				tools.getChildren().removeAll(line_algo_tools, draw_polygons_tools, transformations_tools, tool_transformations, btn_line_clip);
				tools.getChildren().addAll(circle_algo_tools, tool_transformations, btn_line_clip);
			}
		});
		
		btn_transformations.setOnAction(e -> {
			if (selected_tool.equals("Transformations")) {
				tools.getChildren().removeAll(transformations_tools, btn_line_clip);
				tools.getChildren().addAll(btn_line_clip);
				selected_tool = "";
			}else {
				selected_tool = "Transformations";
				isClipping = false;
				drawGraph();
				tools.getChildren().removeAll(line_algo_tools, draw_polygons_tools, circle_algo_tools, btn_line_clip);
				tools.getChildren().addAll(transformations_tools, btn_line_clip);
			}
		});
		
		tools.getChildren().addAll(lbl_tools, tool_line, tool_polygon, tool_circle, tool_transformations, btn_line_clip);
		sidebar.getChildren().add(tools);
		
		return sidebar;
	}
	
	private VBox lineAlgoTools() {
		VBox toolbar = new VBox(22);
		toolbar.getStyleClass().add("toolbar");
		
		VBox choose_algo = new VBox(12);
		Label lbl_choose_algo = new Label("Choose an algorithm");
		
		String algorithms[] = {"DDA", "Bresenham Algorithm"}; 
		ComboBox algo_combo_box = new ComboBox(FXCollections.observableArrayList(algorithms));
		algo_combo_box.setValue("DDA");
		algo_combo_box.setOnAction((event) -> {
		    int selectedIndex = algo_combo_box.getSelectionModel().getSelectedIndex();
		    Object selectedItem = algo_combo_box.getSelectionModel().getSelectedItem();
		    
		    selected_line_algo = (String) algo_combo_box.getValue();
		});
		
		algo_combo_box.getStyleClass().add("combo_box");
		
		choose_algo.getChildren().addAll(lbl_choose_algo, algo_combo_box);
		
		VBox choose_option = new VBox(12);
		Label lbl_choose_options = new Label("Choose an option");
		
		String options[] = {"Solid", "Dashed", "Dotted"}; 
		ComboBox options_combo_box = new ComboBox(FXCollections.observableArrayList(options));
		options_combo_box.setValue("Solid");
		
		options_combo_box.setOnAction((event) -> {
		    int selectedIndex = options_combo_box.getSelectionModel().getSelectedIndex();
		    Object selectedItem = options_combo_box.getSelectionModel().getSelectedItem();
		    
		    selected_line_option = (String) options_combo_box.getValue();
		});
		
		options_combo_box.getStyleClass().add("combo_box");
		
		choose_option.getChildren().addAll(lbl_choose_options, options_combo_box);
		
		VBox line_thickness = new VBox(12);
		
		Label lbl_line_thickness = new Label("Thickness");
		
		thickness_picker = new Spinner(1, 3, 1);
		thickness_picker.setEditable(true);
		thickness_picker.setPrefSize(50, 30);
		
		line_thickness.getChildren().addAll(lbl_line_thickness, thickness_picker);
		//line_thickness.setAlignment(Pos.CENTER_LEFT);
		
		Label lbl_choose_points = new Label("Choose 2 points on the Graph");
		lbl_choose_points.getStyleClass().add("lbl_tip");
		lbl_choose_points.setAlignment(Pos.CENTER);
		
		toolbar.getChildren().addAll(choose_algo, choose_option, line_thickness, lbl_choose_points);
		toolbar.setAlignment(Pos.CENTER_LEFT);
		
		return toolbar;
	}
	
	private double[] addLine() {
		double[] pts = {clicked_points.get(0), clicked_points.get(1), clicked_points.get(2), clicked_points.get(3)};
		return pts;
	}
	
	private VBox drawPolygonsTools() {
		VBox toolbar = new VBox(22);
		toolbar.getStyleClass().add("toolbar_polygon");
		
		Label lbl_choose_options = new Label("Choose options");
		
		VBox polygon_type_vbox = new VBox(12);
		Label lbl_polygon_type = new Label("Type");
		
		String polygon_types[] = {"Regular", "Irregular"}; 
		ComboBox type_combo_box = new ComboBox(FXCollections.observableArrayList(polygon_types));
		type_combo_box.setValue("Regular");
		type_combo_box.setOnAction((event) -> {
		    int selectedIndex = type_combo_box.getSelectionModel().getSelectedIndex();
		    Object selectedItem = type_combo_box.getSelectionModel().getSelectedItem();
		    
		    type = (String) type_combo_box.getValue();
		    for (int i = 0; i < clicked_points.size() / 2; i++) {
		    	sc.getShape_state().pop();
		    	sc.getPoint_circles().remove(sc.getPoint_circles().size() - 1);
		    }
		    clicked_points.clear();
		    
		});
		
		type_combo_box.getStyleClass().add("combo_box");
			
		polygon_type_vbox.getChildren().addAll(lbl_polygon_type, type_combo_box);
		
		VBox polygon_total_sides = new VBox(12);
		
		Label lbl_polygon_sides = new Label("Vertices");
		
		num_sides_picker = new Spinner(3, 20, 1);
		num_sides_picker.setEditable(true);
		num_sides_picker.setPrefSize(50, 30);
		
		polygon_total_sides.getChildren().addAll(lbl_polygon_sides, num_sides_picker);		
		
		VBox polygon_fill = new VBox(12);
		
		HBox polygon_fill_subheading = new HBox(160);
		Label lbl_polygon_fill= new Label("Fill");
		cb_no_fill = new CheckBox("No Fill");
		cb_no_fill.setSelected(true);
		cb_no_fill.getStyleClass().add("check-box-fill");
		
		polygon_fill_subheading.getChildren().addAll(lbl_polygon_fill, cb_no_fill);
		polygon_fill_subheading.setAlignment(Pos.CENTER_LEFT);
		
		final ColorPicker color_picker = new ColorPicker(null);
		
		color_picker.setOnAction(new EventHandler() {
		     public void handle(Event t) {
		    	 color = color_picker.getValue();
		    	 cb_no_fill.setSelected(false);
		     }
		 });
		//color_picker.getStyleClass().add("color_picker");

		polygon_fill.getChildren().addAll(polygon_fill_subheading, color_picker);
		
		VBox polygon_stroke = new VBox(12);
		
		Label lbl_polygon_stroke= new Label("Stroke");
			
		stroke_picker = new Spinner(0, 5, 1);
		stroke_picker.setEditable(true);
		stroke_picker.setPrefSize(50, 30);
		
		polygon_stroke.getChildren().addAll(lbl_polygon_stroke, stroke_picker);
		
		VBox polygon_stroke_color_vbox = new VBox(12);
		
		Label lbl_polygon_stroke_color= new Label("Stroke Color");
		
		final ColorPicker color_picker_polygon = new ColorPicker(Color.BLACK);
		
		color_picker_polygon.setOnAction(new EventHandler() {
		     public void handle(Event t) {
		    	 polygon_stroke_color = color_picker_polygon.getValue();
		     }
		 });
		
		color_picker_polygon.getStyleClass().add("color_picker");

		polygon_stroke_color_vbox.getChildren().addAll(lbl_polygon_stroke_color, color_picker_polygon);
		
		VBox polygon_size_box = new VBox(12);
		
		Label lbl_polygon_size= new Label("Length");
		
		size_picker = new Spinner(0, 100, 1);
		size_picker.setEditable(true);
		size_picker.setPrefSize(50, 30);
		
		polygon_size_box.getChildren().addAll(lbl_polygon_size, size_picker);
		
		toolbar.getChildren().addAll(lbl_choose_options, polygon_type_vbox, polygon_total_sides, polygon_fill, polygon_stroke, polygon_stroke_color_vbox, polygon_size_box);
		toolbar.setAlignment(Pos.CENTER_LEFT);
		
		return toolbar;
	}
	
	public ArrayList<Double> getClickedPoints() {
		return clicked_points;
	}

	private VBox circleAlgoTools() {
		VBox toolbar = new VBox(22);
		toolbar.getStyleClass().add("toolbar");
		
		Label lbl_choose_options = new Label("Choose options");
			
		VBox circle_radius = new VBox(12);
		
		Label lbl_circle_radius = new Label("Radius");
		
		radius_picker = new Spinner(1, 100, 4);
		radius_picker.setEditable(true);
		radius_picker.setPrefSize(50, 30);
		
		circle_radius.getChildren().addAll(lbl_circle_radius, radius_picker);
		
		VBox circle_stroke_color = new VBox(12);
		
		Label lbl_circle_stroke= new Label("Stroke Color");
		
		final ColorPicker color_picker_circle = new ColorPicker(Color.BLACK);
		
		color_picker_circle.setOnAction(new EventHandler() {
		     public void handle(Event t) {
		    	 circle_color = color_picker_circle.getValue();
		     }
		 });
		
		color_picker_circle.getStyleClass().add("color_picker");

		circle_stroke_color.getChildren().addAll(lbl_circle_stroke, color_picker_circle);
		
		VBox circle_stroke = new VBox(12);
		
		Label lbl_polygon_stroke= new Label("Stroke");
		
		circle_stroke_picker = new Spinner(0, 5, 1);
		circle_stroke_picker.setEditable(true);
		circle_stroke_picker.setPrefSize(50, 30);
		
		circle_stroke.getChildren().addAll(lbl_polygon_stroke, circle_stroke_picker);
		
		toolbar.getChildren().addAll(lbl_choose_options, circle_radius, circle_stroke, circle_stroke_color);
		toolbar.setAlignment(Pos.CENTER);
		
		return toolbar;
	}
	
	private VBox transformationsTools() {
		VBox toolbar = new VBox(22);
		toolbar.getStyleClass().add("toolbar_transformation");
		
		Label lbl_choose_transformation = new Label("Choose transformation type");
		
		VBox transformation_type_vbox = new VBox(12);
		Label lbl_transformation_type = new Label("Transformation Type");
		
		String transformation_types[] = {"Translation", "Rotation", "Rotation About Fixed Point", "Scale", "Scale About Fixed Point",  "Shear", "Reflection"}; 
		
		VBox transformation_properties_box = new VBox(12);
		
		HBox transformation_properties = new HBox(5);
		
		Label lbl_transformation_properties= new Label("Transformation Properties");
		transformation_properties.getChildren().addAll();
		
		Button btn_transform = new Button("Transform");
		btn_transform.getStyleClass().add("sidebar__btnLarge");
		btn_transform.setPrefSize(50, 20);
		
		Button btn_scanlineFill = new Button("Scanline Fill");
		btn_scanlineFill.getStyleClass().add("sidebar__btnLarge");
		btn_scanlineFill.setPrefSize(50, 20);
		
		transformation_properties_box.getChildren().addAll(lbl_transformation_properties, transformation_properties);
		
		ComboBox<String> type_combo_box = new ComboBox<String>(FXCollections.observableArrayList(transformation_types));
		type_combo_box.setValue("Choose a transformation");
		type_combo_box.setOnAction((event) -> {
			
		    transformation_type = (String) type_combo_box.getValue();
		    for (int i = 0; i < clicked_points.size() / 2; i++) {
		    	sc.getShape_state().pop();
		    	sc.getPoint_circles().remove(sc.getPoint_circles().size() - 1);
		    }
		    clicked_points.clear();
		    current_points_to_transform.clear();
		    
		    switch(transformation_type) {
		    case "Translation":
		    	toolbar.getChildren().removeAll(transformation_properties_box, btn_transform, btn_scanlineFill);
		    	
		    	VBox translateX_box = new VBox(6);
		    	
		    	Label lbl_translateX = new Label("Translate X");
		    	translateX_picker = new Spinner<Integer>(-1000, 1000, 4);
		    	translateX_picker.setEditable(true);
		    	translateX_picker.setPrefSize(50, 30);
		    	translateX_box.getChildren().addAll(lbl_translateX, translateX_picker);
		    	
		    	VBox translateY_box = new VBox(6);
		    	Label lbl_translateY = new Label("Translate Y");
		    	translateY_picker = new Spinner<Integer>(-1000, 1000, 4);
		    	translateY_picker.setEditable(true);
		    	translateY_picker.setPrefSize(20, 20);
		    	translateY_box.getChildren().addAll(lbl_translateY, translateY_picker);
		    	
		    	transformation_properties_box.getChildren().clear();
		    	transformation_properties_box.getChildren().addAll(translateX_box);
		    	transformation_properties_box.getChildren().addAll(translateY_box);
		    	
		    	toolbar.getChildren().addAll(transformation_properties_box, btn_transform, btn_scanlineFill);
		    	break;
		    case "Rotation":
		    	toolbar.getChildren().removeAll(transformation_properties_box, btn_transform, btn_scanlineFill);
		    	
		    	VBox rotation_box = new VBox(6);
		    	
		    	Label lbl_rotation = new Label("Rotation Angle");
		    	rotateAngle_picker = new Spinner<Integer>(-360, 360, 45);
		    	rotateAngle_picker.setEditable(true);
		    	rotateAngle_picker.setPrefSize(50, 30);
		    	rotation_box.getChildren().addAll(lbl_rotation, rotateAngle_picker);
		    	
		    	transformation_properties_box.getChildren().clear();
		    	transformation_properties_box.getChildren().addAll(rotation_box);
		    	
		    	toolbar.getChildren().addAll(transformation_properties_box, btn_transform, btn_scanlineFill);
		    	break;
		    case "Rotation About Fixed Point":
		    	toolbar.getChildren().removeAll(transformation_properties_box, btn_transform, btn_scanlineFill);
		    	
		    	VBox rotation_fixed_point_box = new VBox(6);
		    	
		    	Label lbl_rotate_angle = new Label("Rotation Angle");
		    	rotateAngle_picker = new Spinner<Integer>(-360, 360, 45);
		    	rotateAngle_picker.setEditable(true);
		    	rotateAngle_picker.setPrefSize(50, 30);
		    	rotation_fixed_point_box.getChildren().addAll(lbl_rotate_angle, rotateAngle_picker);
		    	
		    	VBox fixedPointX_box = new VBox(6);
		    	Label lbl_fixedPointX = new Label("Fixed Point X");
		    	fixedPointX_picker = new Spinner<Integer>(-1000, 1000, 4);
		    	fixedPointX_picker.setEditable(true);
		    	fixedPointX_picker.setPrefSize(20, 20);
		    	fixedPointX_box.getChildren().addAll(lbl_fixedPointX, fixedPointX_picker);
		    	
		    	VBox fixedPointY_box = new VBox(6);
		    	Label lbl_fixedPointY = new Label("Fixed Point Y");
		    	fixedPointY_picker = new Spinner<Integer>(-1000, 1000, 4);
		    	fixedPointY_picker.setEditable(true);
		    	fixedPointY_picker.setPrefSize(20, 20);
		    	fixedPointY_box.getChildren().addAll(lbl_fixedPointY, fixedPointY_picker);
		    	
		    	transformation_properties_box.getChildren().clear();
		    	transformation_properties_box.getChildren().addAll(rotation_fixed_point_box, fixedPointX_box, fixedPointY_box);
		    	
		    	toolbar.getChildren().addAll(transformation_properties_box, btn_transform, btn_scanlineFill);
		    	break;
		    case "Scale":
		    	toolbar.getChildren().removeAll(transformation_properties_box, btn_transform, btn_scanlineFill);
		    	
		    	VBox scaleFactorX_box = new VBox(6);
		    	
		    	Label lbl_scaleFactorX = new Label("Scale Factor X");
		    	scaleFactorX_picker = new Spinner<Integer>(1, 20, 3);
		    	scaleFactorX_picker.setEditable(true);
		    	scaleFactorX_picker.setPrefSize(50, 30);
		    	scaleFactorX_box.getChildren().addAll(lbl_scaleFactorX, scaleFactorX_picker);
		    	
		    	VBox scaleFactorY_box = new VBox(6);
		    	Label lbl_scaleFactorY = new Label("Scale Factor Y");
		    	scaleFactorY_picker = new Spinner<Integer>(1, 20, 3);
		    	scaleFactorY_picker.setEditable(true);
		    	scaleFactorY_picker.setPrefSize(20, 20);
		    	scaleFactorY_box.getChildren().addAll(lbl_scaleFactorY, scaleFactorY_picker);
		    	
		    	transformation_properties_box.getChildren().clear();
		    	transformation_properties_box.getChildren().addAll(scaleFactorX_box, scaleFactorY_box);
		    	
		    	toolbar.getChildren().addAll(transformation_properties_box, btn_transform, btn_scanlineFill);
		    	break;
		    case "Scale About Fixed Point":
		    	toolbar.getChildren().removeAll(transformation_properties_box, btn_transform, btn_scanlineFill);
		    	
		    	VBox scaleFixedFactorX_box = new VBox(6);
		    	
		    	Label lbl_scaleFixedFactorX = new Label("Scale Factor X");
		    	scaleFactorX_picker = new Spinner<Integer>(1, 20, 3);
		    	scaleFactorX_picker.setEditable(true);
		    	scaleFactorX_picker.setPrefSize(50, 30);
		    	scaleFixedFactorX_box.getChildren().addAll(lbl_scaleFixedFactorX, scaleFactorX_picker);
		    	
		    	VBox scaleFixedFactorY_box = new VBox(6);
		    	Label lbl_scaleFixedFactorY = new Label("Scale Factor Y");
		    	scaleFactorY_picker = new Spinner<Integer>(1, 20, 3);
		    	scaleFactorY_picker.setEditable(true);
		    	scaleFactorY_picker.setPrefSize(20, 20);
		    	scaleFixedFactorY_box.getChildren().addAll(lbl_scaleFixedFactorY, scaleFactorY_picker);
		    	
		    	fixedPointX_box = new VBox(6);
		    	lbl_fixedPointX = new Label("Fixed Point X");
		    	fixedPointX_picker = new Spinner<Integer>(-1000, 1000, 4);
		    	fixedPointX_picker.setEditable(true);
		    	fixedPointX_picker.setPrefSize(20, 20);
		    	fixedPointX_box.getChildren().addAll(lbl_fixedPointX, fixedPointX_picker);
		    	
		    	fixedPointY_box = new VBox(6);
		    	lbl_fixedPointY = new Label("Fixed Point Y");
		    	fixedPointY_picker = new Spinner<Integer>(-1000, 1000, 4);
		    	fixedPointY_picker.setEditable(true);
		    	fixedPointY_picker.setPrefSize(20, 20);
		    	fixedPointY_box.getChildren().addAll(lbl_fixedPointY, fixedPointY_picker);
		    	
		    	transformation_properties_box.getChildren().clear();
		    	transformation_properties_box.getChildren().addAll(scaleFixedFactorX_box, scaleFixedFactorY_box, fixedPointX_box, fixedPointY_box);
		    	
		    	toolbar.getChildren().addAll(transformation_properties_box, btn_transform, btn_scanlineFill);
		    	break;
		    case "Shear":
		    	toolbar.getChildren().removeAll(transformation_properties_box, btn_transform, btn_scanlineFill);
		    	
		    	VBox shear_relative_to_box = new VBox(6);
		    	
		    	Label lbl_shear_relative_to= new Label("Shear Relative To: ");
		    	ToggleGroup group = new ToggleGroup();

		    	HBox shear_radio_button_box = new HBox(40);
		    	rb1 = new RadioButton("X");
		    	rb1.setTextFill(Color.WHITE);
		    	rb1.setToggleGroup(group);
		    	rb1.setSelected(true);

		    	rb2 = new RadioButton("Y");
		    	rb2.setTextFill(Color.WHITE);
		    	rb2.setToggleGroup(group);
		    	
		    	shear_radio_button_box.getChildren().addAll(rb1, rb2);
		    	shear_relative_to_box.getChildren().addAll(lbl_shear_relative_to, shear_radio_button_box);
		    	
		    	VBox shear_value_box = new VBox(6);
		    	Label lbl_shear_value = new Label("Shear Value");
		    	shearValue_picker = new Spinner<Integer>(-100, 100, 4);
		    	shearValue_picker.setEditable(true);
		    	shearValue_picker.setPrefSize(20, 20);
		    	shear_value_box.getChildren().addAll(lbl_shear_value, shearValue_picker);
		    	
		    	transformation_properties_box.getChildren().clear();
		    	transformation_properties_box.getChildren().addAll(shear_relative_to_box, shear_value_box);
		    	
		    	toolbar.getChildren().addAll(transformation_properties_box, btn_transform, btn_scanlineFill);
		    	break;
		    case "Reflection":
		    	toolbar.getChildren().removeAll(transformation_properties_box, btn_transform, btn_scanlineFill);
		    	
		    	VBox reflection_relative_to_box = new VBox(6);
		    	
		    	Label lbl_reflection_relative_to= new Label("Reflection Relative To: ");
		    	ToggleGroup reflection_radio_button_group = new ToggleGroup();

		    	HBox reflection_radio_button_box = new HBox(40);
		    	rb_reflection_X = new RadioButton("X");
		    	rb_reflection_X.setTextFill(Color.WHITE);
		    	rb_reflection_X.setToggleGroup(reflection_radio_button_group);
		    	rb_reflection_X.setSelected(true);

		    	rb_reflection_Y = new RadioButton("Y");
		    	rb_reflection_Y.setTextFill(Color.WHITE);
		    	rb_reflection_Y.setToggleGroup(reflection_radio_button_group);
		    	
		    	rb_reflection_XY = new RadioButton("XY");
		    	rb_reflection_XY.setTextFill(Color.WHITE);
		    	rb_reflection_XY.setToggleGroup(reflection_radio_button_group);
		    	
		    	rb_reflection_YX = new RadioButton("YX");
		    	rb_reflection_YX.setTextFill(Color.WHITE);
		    	rb_reflection_YX.setToggleGroup(reflection_radio_button_group);
		    	
		    	reflection_radio_button_box.getChildren().addAll(rb_reflection_X, rb_reflection_Y, rb_reflection_XY, rb_reflection_YX);
		    	reflection_relative_to_box.getChildren().addAll(lbl_reflection_relative_to, reflection_radio_button_box);
		    	
//		    	VBox shear_value_box = new VBox(6);
//		    	Label lbl_shear_value = new Label("Shear Value");
//		    	shearValue_picker = new Spinner<Integer>(-100, 100, 4);
//		    	shearValue_picker.setEditable(true);
//		    	shearValue_picker.setPrefSize(20, 20);
//		    	shear_value_box.getChildren().addAll(lbl_shear_value, shearValue_picker);
		    	
		    	transformation_properties_box.getChildren().clear();
		    	transformation_properties_box.getChildren().addAll(reflection_relative_to_box);
		    	
		    	toolbar.getChildren().addAll(transformation_properties_box, btn_transform, btn_scanlineFill);
		    	break;
		    default:
		    	break;
		    }
		    
		});
		
		type_combo_box.getStyleClass().add("combo_box");
			
		transformation_type_vbox.getChildren().addAll(lbl_transformation_type, type_combo_box);
		
		VBox polygon_total_sides = new VBox(12);
		
		Label lbl_polygon_sides = new Label("Vertices");
		
		sides_picker = new Spinner<Integer>(3, 20, 1);
		sides_picker.setEditable(true);
		sides_picker.setPrefSize(50, 30);
		
		polygon_total_sides.getChildren().addAll(lbl_polygon_sides, sides_picker);		
		
		VBox polygon_fill = new VBox(12);
		
		HBox polygon_fill_subheading = new HBox(160);
		Label lbl_polygon_fill= new Label("Fill");
		cb_no_fill = new CheckBox("No Fill");
		cb_no_fill.setSelected(false);
		cb_no_fill.getStyleClass().add("check-box-fill");
		
		polygon_fill_subheading.getChildren().addAll(lbl_polygon_fill, cb_no_fill);
		polygon_fill_subheading.setAlignment(Pos.CENTER_LEFT);
		
		final ColorPicker color_picker = new ColorPicker(Color.BLUE);
		color = color_picker.getValue();
		
		color_picker.setOnAction(new EventHandler() {
		     public void handle(Event t) {
		    	 color = color_picker.getValue();
		    	 cb_no_fill.setSelected(false);
		     }
		 });
		//color_picker.getStyleClass().add("color_picker");

		polygon_fill.getChildren().addAll(polygon_fill_subheading, color_picker);
		
		VBox polygon_stroke = new VBox(12);
		
		Label lbl_polygon_stroke= new Label("Stroke");
			
		stroke_picker = new Spinner(0, 5, 1);
		stroke_picker.setEditable(true);
		stroke_picker.setPrefSize(50, 30);
		
		polygon_stroke.getChildren().addAll(lbl_polygon_stroke, stroke_picker);
		
		VBox polygon_stroke_color_vbox = new VBox(12);
		
		Label lbl_polygon_stroke_color= new Label("Stroke Color");
		
		final ColorPicker color_picker_polygon = new ColorPicker(Color.BLACK);
		
		color_picker_polygon.setOnAction(new EventHandler() {
		     public void handle(Event t) {
		    	 polygon_stroke_color = color_picker_polygon.getValue();
		     }
		 });
		
		color_picker_polygon.getStyleClass().add("color_picker");

		polygon_stroke_color_vbox.getChildren().addAll(lbl_polygon_stroke_color, color_picker_polygon);
		
		btn_transform.setOnAction(e -> {
			if (transformation_type.equals("Translation")) playTranslate();
			else if (transformation_type.equals("Rotation")) playRotation();
			else if (transformation_type.equals("Rotation About Fixed Point")) playRotationAboutFixedPoint();
			else if (transformation_type.equals("Scale")) playScaleStandard();
			else if (transformation_type.equals("Scale About Fixed Point")) playScaleFixedPoint();
			else if (transformation_type.equals("Reflection"))playReflection();
			else if (transformation_type.equals("Shear")) {
				if (rb1.isSelected()) playShearX();
				else playShearY();
			}
//			

		});
		
		btn_scanlineFill.setOnAction(e -> {
			fillScanline();
//			lineClipping();
		});

		toolbar.getChildren().addAll(lbl_choose_transformation, transformation_type_vbox, polygon_total_sides, polygon_fill, polygon_stroke, polygon_stroke_color_vbox, transformation_properties_box, btn_transform, btn_scanlineFill);
		toolbar.setAlignment(Pos.CENTER_LEFT);
		
		return toolbar;
	}
	
	public void drawClippingWindow() {
		grid.getChildren().remove(graph);
        graph = sc.drawClippingWindow();
        addMouseScrolling(graph);
        addMouseClickListener(graph);
        GridPane.setConstraints(graph, 1, 0);
        grid.getChildren().add(graph);		
	}
	
	public void lineClipping() {
		isClipping = true;
		drawClippingWindow();
	}
	
	public void fillScanline() {
		ArrayList<DrawPolygon> polygons = sc.getShapes();
		if (polygons.size() < 0) return;
		DrawPolygon polygon = polygons.get(polygons.size() - 1);
		polygon.setFillColor(color);
		polygon.reworkScanlinePixelsToFill(sc.getRealPosition(polygon.getPoints()));
		polygon.setIsAnimationOn(true);
		
		playScanline(polygon);
	}
	
	public void playScanline(DrawPolygon polygon) {
		Timer timer = new Timer();
	    timer.scheduleAtFixedRate(new TimerTask() {
	    	
	    	int totalIncrements = polygon.getScanlineFillPixels().size();
	    	int currentIncrement = 0;
	    	
	    	ArrayList<Line> lines = new ArrayList<Line>();
	   	
	        @Override
	        public void run() {
	        	javafx.application.Platform.runLater(new Runnable() {
	                @Override
	                public void run() {
	                	if (currentIncrement < totalIncrements) {
		                	ArrayList<Integer> scanlineY = polygon.getScanlineFillPixels().get(currentIncrement);
		                	
	    					int yIndex = scanlineY.get(0);
	    					
	    					drawGraph();
	    					
	    					for (Line line : lines) {
	    						graph.getChildren().add(line);
	    					}
	    					
	    					for  (int i = 1; i < scanlineY.size();  i+=2) {
	    						int sX = scanlineY.get(i);
	    						int eX = scanlineY.get(i+1);
	    						Line line = new Line(sX, yIndex, eX, yIndex);
	    						line.setStroke(polygon.getFillColor());
	    						graph.getChildren().add(line);
	    						lines.add(line);
	    					}		
	
		        			currentIncrement++;
		                }
	                }
	            });
	        	
	        	if (currentIncrement == totalIncrements) {
	        		polygon.setIsAnimationOn(false);
	        		this.cancel();
	        	}
	        }
	    }, 0, 50);
	}
	
	public void playTranslate(){
		Timer timer = new Timer();
	    timer.scheduleAtFixedRate(new TimerTask() {
	    	int translateX = translateX_picker.getValue();
			int translateY = translateY_picker.getValue();
			float incrementX = (float) .1 * translateX;
			float incrementY = (float) .1 * translateY;
			float currentTranslateX = incrementX;
			float currentTranslateY = incrementY;
			
			ArrayList<Double> transformed_points = Transformations.Translate(current_points_to_transform, currentTranslateX, currentTranslateY);
			
			ArrayList<DrawPolygon> previousPolygons = sc.getShapes();
			DrawPolygon previousPolygon = previousPolygons.get(previousPolygons.size()-1);
			
	        @Override
	        public void run() {
	        	javafx.application.Platform.runLater(new Runnable() {
	                @Override
	                public void run() {
	                	if (currentTranslateX != incrementX || currentTranslateY != incrementY) {
	        				sc.remove_last_irregular_shape_without_point_circles();
	        			}else {
	        				sc.remove_last_irregular_shape();	
	        			}
	                	
	                	DrawPolygon polygon = new DrawPolygon(transformed_points.size()/2, stroke_picker.getValue(), null, "Irregular", size_picker.getValue(), polygon_stroke_color, 0, 0, transformed_points);
	        			polygon.setHasPointCircles(false);
	                	if (previousPolygon.getScanlineFillPixels().size() > 0) {
	        				polygon.setFillColor(color);
	        				polygon.setScanlineFillPixels(previousPolygon.getScanlineFillPixels());
	        			}
	                	sc.addShape(polygon);
	        				
	                	currentTranslateX += incrementX;
	        			currentTranslateY += incrementY;
	        			
	        			if (currentTranslateX > translateX) currentTranslateX = translateX;
	        			if (currentTranslateY > translateY) currentTranslateY = translateY;
	        			
	        			transformed_points = Transformations.Translate(transformed_points, incrementX, incrementY);
	        		
	        			drawGraph();
	                }
	            });
	        	
	        	if (currentTranslateX == translateX && currentTranslateY == translateY) {
	        		current_points_to_transform.clear();
	        		this.cancel();
	        	}
	        }
	    }, 0, 100);		
	}
	
	public void playRotation(){
		Timer timer = new Timer();
	    timer.scheduleAtFixedRate(new TimerTask() {
	    	int rotationAngle = rotateAngle_picker.getValue();
			float incrementRotation = (float) 1;
			float currentRotation = incrementRotation;
			
			ArrayList<Double> transformed_points = Transformations.RotateStandard(current_points_to_transform, incrementRotation);
			
			ArrayList<DrawPolygon> previousPolygons = sc.getShapes();
			DrawPolygon previousPolygon = previousPolygons.get(previousPolygons.size()-1);
	    	
	        @Override
	        public void run() {
	        	javafx.application.Platform.runLater(new Runnable() {
	                @Override
	                public void run() {
	                	if (currentRotation != incrementRotation) {
	        				sc.remove_last_irregular_shape_without_point_circles();
	        			}else {
	        				sc.remove_last_irregular_shape();	
	        			}
	                	
	                	DrawPolygon polygon = new DrawPolygon(transformed_points.size()/2, stroke_picker.getValue(), null, "Irregular", size_picker.getValue(), polygon_stroke_color, 0, 0, transformed_points);
	                	polygon.setHasPointCircles(false);
	                	
	                	if (previousPolygon.getScanlineFillPixels().size() > 0) {
	                		polygon.setFillColor(color);
	        				polygon.setScanlineFillPixels(previousPolygon.getScanlineFillPixels());
	        			}
	                	
	                	sc.addShape(polygon);
	        				
	                	currentRotation += incrementRotation;
	        			
	        			if (currentRotation > rotationAngle) currentRotation = rotationAngle;
	        			
	        			transformed_points = Transformations.RotateStandard(transformed_points, incrementRotation);
	        		
	        			drawGraph();
	                }
	            });
	        	
	        	if (currentRotation == rotationAngle) {
	        		current_points_to_transform.clear();
	        		this.cancel();
	        	}
	        }
	    }, 0, 100);		
	}

	public void playRotationAboutFixedPoint(){
		Timer timer = new Timer();
	    timer.scheduleAtFixedRate(new TimerTask() {
	    	int rotationAngle = rotateAngle_picker.getValue();
			float incrementRotation = (float) 1;
			float currentRotation = incrementRotation;
			int centerX = fixedPointX_picker.getValue();
			int centerY = fixedPointY_picker.getValue();
			
			
			ArrayList<Double> transformed_points = Transformations.RotateFixedPoint(current_points_to_transform, incrementRotation, centerX, centerY);
	    	
			ArrayList<DrawPolygon> previousPolygons = sc.getShapes();
			DrawPolygon previousPolygon = previousPolygons.get(previousPolygons.size()-1);
			
	        @Override
	        public void run() {
	        	javafx.application.Platform.runLater(new Runnable() {
	                @Override
	                public void run() {
	                	if (currentRotation != incrementRotation) {
	        				sc.remove_last_irregular_shape_without_point_circles();
	        			}else {
	        				sc.remove_last_irregular_shape();	
	        			}
	                	
	                	DrawPolygon polygon = new DrawPolygon(transformed_points.size()/2, stroke_picker.getValue(),  null , "Irregular", size_picker.getValue(), polygon_stroke_color, 0, 0, transformed_points);
	                	polygon.setHasPointCircles(false);
	                	
	                	if (previousPolygon.getScanlineFillPixels().size() > 0) {
	                		polygon.setFillColor(color);
	        				polygon.setScanlineFillPixels(previousPolygon.getScanlineFillPixels());
	        			}
	                	
	                	sc.addShape(polygon);
	        				
	                	currentRotation += incrementRotation;
	        			
	        			if (currentRotation > rotationAngle) currentRotation = rotationAngle;
	        			
	        			transformed_points = Transformations.RotateFixedPoint(transformed_points, incrementRotation, centerX, centerY);
	        		
	        			drawGraph();
	                }
	            });
	        	
	        	if (currentRotation == rotationAngle) {
	        		current_points_to_transform.clear();
	        		this.cancel();
	        	}
	        }
	    }, 0, 100);		
	}
	
	public void playScaleStandard(){
		Timer timer = new Timer();
	    timer.scheduleAtFixedRate(new TimerTask() {
	    	float scaleFactorX = scaleFactorX_picker.getValue();
	    	float scaleFactorY = scaleFactorY_picker.getValue();
	    	double totalIncrements = 20;
	    	double currentIncrement = 0;
	    	
			double incrementScaleFactorX = Math.pow(scaleFactorX, (1.0 /  totalIncrements));
			double incrementScaleFactorY = Math.pow(scaleFactorY, (1.0 /  totalIncrements));
			
			ArrayList<Double> transformed_points = Transformations.scaleStandard(current_points_to_transform, incrementScaleFactorX, incrementScaleFactorY);
	    	
			ArrayList<DrawPolygon> previousPolygons = sc.getShapes();
			DrawPolygon previousPolygon = previousPolygons.get(previousPolygons.size()-1);
			
	        @Override
	        public void run() {
	        	javafx.application.Platform.runLater(new Runnable() {
	                @Override
	                public void run() {
	                	if (currentIncrement != 0 ) {
	        				sc.remove_last_irregular_shape_without_point_circles();
	        			}else {
	        				sc.remove_last_irregular_shape();
	        			}

	                	currentIncrement++;
	                	DrawPolygon polygon = new DrawPolygon(transformed_points.size()/2, stroke_picker.getValue(), null, "Irregular", size_picker.getValue(), polygon_stroke_color, 0, 0, transformed_points);
	                	polygon.setHasPointCircles(false);
	                	
	                	if (previousPolygon.getScanlineFillPixels().size() > 0) {
	                		polygon.setFillColor(color);
	        				polygon.setScanlineFillPixels(previousPolygon.getScanlineFillPixels());
	        			}
	                	
	                	sc.addShape(polygon);
	        				
	        			transformed_points = Transformations.scaleStandard(transformed_points, incrementScaleFactorX, incrementScaleFactorY);
	        		
	        			drawGraph();
	                }
	            });
	        	
	        	if (currentIncrement == totalIncrements) {
	        		current_points_to_transform.clear();
	        		this.cancel();
	        	}
	        }
	    }, 0, 100);		
	}
	
	//Verify correct transformation
	public void playScaleFixedPoint(){
		Timer timer = new Timer();
	    timer.scheduleAtFixedRate(new TimerTask() {
	    	float scaleFactorX = scaleFactorX_picker.getValue();
	    	float scaleFactorY = scaleFactorY_picker.getValue();
	    	double totalIncrements = 20;
	    	double currentIncrement = 0;
	    	
	    	int centerX = fixedPointX_picker.getValue();
			int centerY = fixedPointY_picker.getValue();
	    	
			double incrementScaleFactorX = Math.pow(scaleFactorX, (1.0 /  totalIncrements));
			double incrementScaleFactorY = Math.pow(scaleFactorY, (1.0 /  totalIncrements));
			
			ArrayList<Double> transformed_points = Transformations.scaleFixedPoint(current_points_to_transform, incrementScaleFactorX, incrementScaleFactorY, centerX, centerY);
	    	
			ArrayList<DrawPolygon> previousPolygons = sc.getShapes();
			DrawPolygon previousPolygon = previousPolygons.get(previousPolygons.size()-1);
			
	        @Override
	        public void run() {
	        	javafx.application.Platform.runLater(new Runnable() {
	                @Override
	                public void run() {
	                	if (currentIncrement != 0 ) {
	        				sc.remove_last_irregular_shape_without_point_circles();
	        			}else {
	        				sc.remove_last_irregular_shape();
	        			}

	                	currentIncrement++;
	                	DrawPolygon polygon = new DrawPolygon(transformed_points.size()/2, stroke_picker.getValue(), null, "Irregular", size_picker.getValue(), polygon_stroke_color, 0, 0, transformed_points);
	                	polygon.setHasPointCircles(false);
	                	
	                	if (previousPolygon.getScanlineFillPixels().size() > 0) {
	                		polygon.setFillColor(color);
	        				polygon.setScanlineFillPixels(previousPolygon.getScanlineFillPixels());
	        			}
	                	
	                	sc.addShape(polygon);
	        				
	        			transformed_points = Transformations.scaleFixedPoint(transformed_points, incrementScaleFactorX, incrementScaleFactorY, centerX, centerY);
	        		
	        			drawGraph();
	                }
	            });
	        	
	        	if (currentIncrement == totalIncrements) {
	        		current_points_to_transform.clear();
	        		this.cancel();
	        	}
	        }
	    }, 0, 100);		
	}
	
	public void playShearX(){
		Timer timer = new Timer();
	    timer.scheduleAtFixedRate(new TimerTask() {
	    	
	    	double shear_value = shearValue_picker.getValue();
	    	
	    	double totalIncrements = 50;
	    	double currentIncrement = 0;
	    		
//	    	double incrementShearFactor = Math.pow(shear_value, (1.0 /  totalIncrements));
			
			ArrayList<Double> transformed_points = Transformations.shearX(current_points_to_transform, shear_value, totalIncrements);
	    	
			ArrayList<DrawPolygon> previousPolygons = sc.getShapes();
			DrawPolygon previousPolygon = previousPolygons.get(previousPolygons.size()-1);
			
	        @Override
	        public void run() {
	        	javafx.application.Platform.runLater(new Runnable() {
	                @Override
	                public void run() {
	                	if (currentIncrement != 0 ) {
	        				sc.remove_last_irregular_shape_without_point_circles();
	        			}else {
	        				sc.remove_last_irregular_shape();
	        			}

	                	currentIncrement++;
	                	DrawPolygon polygon = new DrawPolygon(transformed_points.size()/2, stroke_picker.getValue(), null, "Irregular", size_picker.getValue(), polygon_stroke_color, 0, 0, transformed_points);
	                	polygon.setHasPointCircles(false);
	                	
	                	if (previousPolygon.getScanlineFillPixels().size() > 0) {
	                		polygon.setFillColor(color);
	        				polygon.setScanlineFillPixels(previousPolygon.getScanlineFillPixels());
	        			}
	                	
	                	sc.addShape(polygon);
	        				
	        			transformed_points = Transformations.shearX(transformed_points, shear_value, totalIncrements);
	        		
	        			drawGraph();
	                }
	            });
	        	
	        	if (currentIncrement == totalIncrements) {
	        		current_points_to_transform.clear();
	        		this.cancel();
	        	}
	        }
	    }, 0, 100);		
	}
	
	public void playShearY(){
		Timer timer = new Timer();
	    timer.scheduleAtFixedRate(new TimerTask() {
	    	double shear_value = shearValue_picker.getValue();
	    	
	    	double totalIncrements = 50;
	    	double currentIncrement = 0;
	    					
			ArrayList<Double> transformed_points = Transformations.shearY(current_points_to_transform, shear_value, totalIncrements);
	    	
			ArrayList<DrawPolygon> previousPolygons = sc.getShapes();
			DrawPolygon previousPolygon = previousPolygons.get(previousPolygons.size()-1);
			
	        @Override
	        public void run() {
	        	javafx.application.Platform.runLater(new Runnable() {
	                @Override
	                public void run() {
	                	if (currentIncrement != 0 ) {
	        				sc.remove_last_irregular_shape_without_point_circles();
	        			}else {
	        				sc.remove_last_irregular_shape();
	        			}

	                	currentIncrement++;
	                	DrawPolygon polygon = new DrawPolygon(transformed_points.size()/2, stroke_picker.getValue(), null, "Irregular", size_picker.getValue(), polygon_stroke_color, 0, 0, transformed_points);
	                	polygon.setHasPointCircles(false);
	                	
	                	if (previousPolygon.getScanlineFillPixels().size() > 0) {
	                		polygon.setFillColor(color);
	        				polygon.setScanlineFillPixels(previousPolygon.getScanlineFillPixels());
	        			}
	                	
	                	sc.addShape(polygon);
	        				
	        			transformed_points = Transformations.shearY(transformed_points, shear_value, totalIncrements);
	        		
	        			drawGraph();
	                }
	            });
	        	
	        	if (currentIncrement == totalIncrements) {
	        		current_points_to_transform.clear();
	        		this.cancel();
	        	}
	        }
	    }, 0, 100);		
	}
	
	public void playReflection(){
		ArrayList<Double> transformed_points;
		if (rb_reflection_X.isSelected())  transformed_points = Transformations.reflectX(current_points_to_transform);
		else if (rb_reflection_Y.isSelected())  transformed_points = Transformations.reflectY(current_points_to_transform);
		else if (rb_reflection_XY.isSelected())  transformed_points = Transformations.reflectXY(current_points_to_transform);
		else transformed_points = Transformations.reflectYX(current_points_to_transform);
		
		sc.remove_last_irregular_shape();
		DrawPolygon polygon = new DrawPolygon(transformed_points.size()/2, stroke_picker.getValue(), null, "Irregular", size_picker.getValue(), polygon_stroke_color, 0, 0, transformed_points);

    	sc.addShape(polygon);
		drawGraph();
	}
	
	//SEE Y NEG X  reflection
	
}
