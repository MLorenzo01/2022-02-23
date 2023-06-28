package it.polito.tdp.yelp.model;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import it.polito.tdp.yelp.db.YelpDao;

public class Model {
	
	YelpDao dao;
	Graph<Review, DefaultWeightedEdge> graph;
	List<Review> best;
	
	public Model() {
		this.dao = new YelpDao();
	}

	public List<String> getCity(){
		List<String> lista = dao.getCity();
		Collections.sort(lista);
		return lista;
	}
	public List<Business> getBusinesses(String s){
		List<Business> lista = dao.getBusiness(s);
		Collections.sort(lista);
		return lista;
	}
	public String CreaGrafo(Business b) {
		graph = new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
		List<Review> vertici = dao.getReviews(b);
		Graphs.addAllVertices(graph, vertici);
		
		for(Review r1: vertici) {
			for(Review r2: vertici) {
				if(!r1.equals(r2)) {
					if(r1.getDate().isBefore(r2.getDate()) && !r1.getDate().equals(r2.getDate())) {
						Graphs.addEdge(graph, r1, r2, ChronoUnit.DAYS.between(r2.getDate(), r1.getDate()));
					}
				}
			}
		}
		return "Il grafo ha " + graph.vertexSet().size() + " vertici e " + graph.edgeSet().size() + " archi\n";
	}
	
	public String getMax() {
		Review best = null;
		int max = 0;
		
		for(Review r: graph.vertexSet()) {
			if(Graphs.successorListOf(graph, r).size() > max) {
				max = Graphs.successorListOf(graph, r).size();
				best = r;
			}
		}
		return best.getReviewId() + "   ARCHI USCENTI: " + max;
	}
	public String trovaPercorso(){
		best = new ArrayList<>();
		List<Review> parziale = new ArrayList<>();
		cerca(parziale);
		String soluzione = "La sequenza è:\n";
		for(Review r: best) {
			soluzione += r.getReviewId() + "\n";
		}
		long giorni = ChronoUnit.DAYS.between(best.get(0).getDate(), best.get(best.size()-1).getDate());
		soluzione += "La differenza tra i giorni è di " + giorni;
		return soluzione;
		
	}

	private void cerca(List<Review> parziale) {
		if(best.size() < parziale.size()) {
			best = new ArrayList<>(parziale);
		}
		for(Review r: graph.vertexSet()) {
			if(parziale.size() == 0 || (parziale.get(parziale.size()-1).getStars() <= r.getStars() && !parziale.contains(r)) ) {
				parziale.add(r);
				cerca(parziale);
				parziale.remove(r);
			}
		}
	}
}

