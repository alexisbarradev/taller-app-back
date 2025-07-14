package com.taller.usuarioback.config;

import com.taller.usuarioback.model.EstadoUsuario;
import com.taller.usuarioback.model.RolUsuario;
import com.taller.usuarioback.model.Region;
import com.taller.usuarioback.model.Comuna;
import com.taller.usuarioback.model.Comunidad;
import com.taller.usuarioback.model.Provincia;
import com.taller.usuarioback.repository.EstadoUsuarioRepository;
import com.taller.usuarioback.repository.RolUsuarioRepository;
import com.taller.usuarioback.repository.RegionRepository;
import com.taller.usuarioback.repository.ComunaRepository;
import com.taller.usuarioback.repository.ComunidadRepository;
import com.taller.usuarioback.repository.ProvinciaRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatosIniciales(RolUsuarioRepository rolRepo, EstadoUsuarioRepository estadoRepo) {
        return args -> {
            if (rolRepo.count() == 0) {
                rolRepo.save(new RolUsuario(1L, "ADMIN"));
                rolRepo.save(new RolUsuario(2L, "USER"));
                rolRepo.save(new RolUsuario(3L, "INVITADO"));
            }

            if (estadoRepo.count() == 0) {
                estadoRepo.save(new EstadoUsuario(1L, "ACTIVO"));
                estadoRepo.save(new EstadoUsuario(2L, "INACTIVO"));
                estadoRepo.save(new EstadoUsuario(3L, "BLOQUEADO"));
            }
        };
    }

    @Bean
    CommandLineRunner initGeografia(
        RolUsuarioRepository rolRepo,
        EstadoUsuarioRepository estadoRepo,
        RegionRepository regionRepo,
        ProvinciaRepository provinciaRepo,
        ComunaRepository comunaRepo,
        ComunidadRepository comunidadRepo
    ) {
        return args -> {
            // Inicializar roles y estados si no existen
            if (rolRepo.count() == 0) {
                rolRepo.save(new RolUsuario(1L, "ADMIN"));
                rolRepo.save(new RolUsuario(2L, "USER"));
                rolRepo.save(new RolUsuario(3L, "INVITADO"));
            }

            if (estadoRepo.count() == 0) {
                estadoRepo.save(new EstadoUsuario(1L, "ACTIVO"));
                estadoRepo.save(new EstadoUsuario(2L, "INACTIVO"));
                estadoRepo.save(new EstadoUsuario(3L, "BLOQUEADO"));
            }

            // Inicializar todas las regiones de Chile si no existen
            String[] regionesChile = {
                "Arica y Parinacota",
                "Tarapacá",
                "Antofagasta",
                "Atacama",
                "Coquimbo",
                "Valparaíso",
                "Región del Libertador Gral. Bernardo O'Higgins",
                "Región del Maule",
                "Región del Biobío",
                "Región de La Araucanía",
                "Región de Los Ríos",
                "Región de Los Lagos",
                "Región Aysén del General Carlos Ibáñez del Campo",
                "Región de Magallanes y de la Antártica Chilena",
                "Región Metropolitana de Santiago",
                "Región de Ñuble"
            };
            
            for (String nombreRegion : regionesChile) {
                if (regionRepo.findByNombre(nombreRegion) == null) {
                    Region region = new Region();
                    region.setNombre(nombreRegion);
                    region.setCodigo(nombreRegion.replaceAll("\\s+", "").toLowerCase());
                    regionRepo.save(region);
                }
            }

            // Poblar todas las provincias y comunas de Chile
            poblarGeografiaCompleta(regionRepo, provinciaRepo, comunaRepo);
            
            // Poblar comunidades específicas
            poblarComunidadesEspecificas(regionRepo, provinciaRepo, comunaRepo, comunidadRepo);
        };
    }

    private void poblarGeografiaCompleta(
        RegionRepository regionRepo,
        ProvinciaRepository provinciaRepo,
        ComunaRepository comunaRepo
    ) {
        
        // Arica y Parinacota
        poblarRegion(regionRepo, provinciaRepo, comunaRepo, "Arica y Parinacota", Arrays.asList(
            new ProvinciaData("Arica", Arrays.asList("Arica", "Camarones")),
            new ProvinciaData("Parinacota", Arrays.asList("Putre", "General Lagos"))
        ));

        // Tarapacá
        poblarRegion(regionRepo, provinciaRepo, comunaRepo, "Tarapacá", Arrays.asList(
            new ProvinciaData("Iquique", Arrays.asList("Iquique", "Alto Hospicio")),
            new ProvinciaData("Tamarugal", Arrays.asList("Pozo Almonte", "Camiña", "Colchane", "Huara", "Pica"))
        ));

        // Antofagasta
        poblarRegion(regionRepo, provinciaRepo, comunaRepo, "Antofagasta", Arrays.asList(
            new ProvinciaData("Antofagasta", Arrays.asList("Antofagasta", "Mejillones", "Sierra Gorda", "Taltal")),
            new ProvinciaData("El Loa", Arrays.asList("Calama", "Ollagüe", "San Pedro de Atacama")),
            new ProvinciaData("Tocopilla", Arrays.asList("Tocopilla", "María Elena"))
        ));

        // Atacama
        poblarRegion(regionRepo, provinciaRepo, comunaRepo, "Atacama", Arrays.asList(
            new ProvinciaData("Chañaral", Arrays.asList("Chañaral", "Diego de Almagro")),
            new ProvinciaData("Copiapó", Arrays.asList("Copiapó", "Caldera", "Tierra Amarilla")),
            new ProvinciaData("Huasco", Arrays.asList("Vallenar", "Alto del Carmen", "Freirina", "Huasco"))
        ));

        // Coquimbo
        poblarRegion(regionRepo, provinciaRepo, comunaRepo, "Coquimbo", Arrays.asList(
            new ProvinciaData("Elqui", Arrays.asList("La Serena", "Coquimbo", "Andacollo", "La Higuera", "Paiguano", "Vicuña")),
            new ProvinciaData("Choapa", Arrays.asList("Illapel", "Canela", "Los Vilos", "Salamanca")),
            new ProvinciaData("Limarí", Arrays.asList("Ovalle", "Combarbalá", "Monte Patria", "Punitaqui", "Río Hurtado"))
        ));

        // Valparaíso
        poblarRegion(regionRepo, provinciaRepo, comunaRepo, "Valparaíso", Arrays.asList(
            new ProvinciaData("Valparaíso", Arrays.asList("Valparaíso", "Casablanca", "Concón", "Juan Fernández", "Puchuncaví", "Quintero", "Viña del Mar")),
            new ProvinciaData("Isla de Pascua", Arrays.asList("Isla de Pascua")),
            new ProvinciaData("Los Andes", Arrays.asList("Los Andes", "Calle Larga", "Rinconada", "San Esteban")),
            new ProvinciaData("Petorca", Arrays.asList("La Ligua", "Cabildo", "Papudo", "Petorca", "Zapallar")),
            new ProvinciaData("Quillota", Arrays.asList("Quillota", "Calera", "Hijuelas", "La Cruz", "Nogales")),
            new ProvinciaData("San Antonio", Arrays.asList("San Antonio", "Algarrobo", "Cartagena", "El Quisco", "El Tabo", "Santo Domingo")),
            new ProvinciaData("San Felipe de Aconcagua", Arrays.asList("San Felipe", "Catemu", "Llaillay", "Panquehue", "Putaendo", "Santa María")),
            new ProvinciaData("Marga Marga", Arrays.asList("Quilpué", "Limache", "Olmué", "Villa Alemana"))
        ));

        // Metropolitana de Santiago
        poblarRegion(regionRepo, provinciaRepo, comunaRepo, "Región Metropolitana de Santiago", Arrays.asList(
            new ProvinciaData("Santiago", Arrays.asList("Santiago", "Cerrillos", "Cerro Navia", "Conchalí", "El Bosque", "Estación Central", "Huechuraba", "Independencia", "La Cisterna", "La Florida", "La Granja", "La Pintana", "La Reina", "Las Condes", "Lo Barnechea", "Lo Espejo", "Lo Prado", "Macul", "Maipú", "Ñuñoa", "Pedro Aguirre Cerda", "Peñalolén", "Providencia", "Pudahuel", "Quilicura", "Quinta Normal", "Recoleta", "Renca", "San Joaquín", "San Miguel", "San Ramón", "Vitacura")),
            new ProvinciaData("Cordillera", Arrays.asList("Puente Alto", "Pirque", "San José de Maipo")),
            new ProvinciaData("Maipo", Arrays.asList("San Bernardo", "Buin", "Calera de Tango", "Paine")),
            new ProvinciaData("Melipilla", Arrays.asList("Melipilla", "Alhué", "Curacaví", "María Pinto", "San Pedro")),
            new ProvinciaData("Talagante", Arrays.asList("Talagante", "El Monte", "Isla de Maipo", "Padre Hurtado", "Peñaflor"))
        ));

        // O'Higgins
        poblarRegion(regionRepo, provinciaRepo, comunaRepo, "Región del Libertador Gral. Bernardo O'Higgins", Arrays.asList(
            new ProvinciaData("Cachapoal", Arrays.asList("Rancagua", "Codegua", "Coinco", "Coltauco", "Doñihue", "Graneros", "Las Cabras", "Machalí", "Malloa", "Mostazal", "Olivar", "Peumo", "Pichidegua", "Quinta de Tilcoco", "Rengo", "Requínoa", "San Vicente")),
            new ProvinciaData("Cardenal Caro", Arrays.asList("Pichilemu", "La Estrella", "Litueche", "Marchihue", "Navidad", "Paredones")),
            new ProvinciaData("Colchagua", Arrays.asList("San Fernando", "Chépica", "Chimbarongo", "Lolol", "Nancagua", "Palmilla", "Peralillo", "Placilla", "Pumanque", "Santa Cruz"))
        ));

        // Maule
        poblarRegion(regionRepo, provinciaRepo, comunaRepo, "Región del Maule", Arrays.asList(
            new ProvinciaData("Talca", Arrays.asList("Talca", "Constitución", "Curepto", "Empedrado", "Maule", "Pelarco", "Pencahue", "Río Claro", "San Clemente", "San Rafael")),
            new ProvinciaData("Cauquenes", Arrays.asList("Cauquenes", "Chanco", "Pelluhue")),
            new ProvinciaData("Curicó", Arrays.asList("Curicó", "Hualañé", "Licantén", "Molina", "Rauco", "Romeral", "Sagrada Familia", "Teno", "Vichuquén")),
            new ProvinciaData("Linares", Arrays.asList("Linares", "Colbún", "Longaví", "Parral", "Retiro", "San Javier", "Villa Alegre", "Yerbas Buenas"))
        ));

        // Ñuble
        poblarRegion(regionRepo, provinciaRepo, comunaRepo, "Región de Ñuble", Arrays.asList(
            new ProvinciaData("Diguillín", Arrays.asList("Chillán", "Chillán Viejo", "Bulnes", "Coihueco", "El Carmen", "Pemuco", "Pinto", "Quillón", "Ránquil", "San Ignacio", "Yungay")),
            new ProvinciaData("Itata", Arrays.asList("Cobquecura", "Coelemu", "Ninhue", "Portezuelo", "Quirihue", "Ránquil", "Treguaco")),
            new ProvinciaData("Punilla", Arrays.asList("Coelemu", "Ñiquén", "San Carlos", "San Fabián", "San Nicolás"))
        ));

        // Biobío
        poblarRegion(regionRepo, provinciaRepo, comunaRepo, "Región del Biobío", Arrays.asList(
            new ProvinciaData("Concepción", Arrays.asList("Concepción", "Coronel", "Chiguayante", "Florida", "Hualpén", "Hualqui", "Lota", "Penco", "San Pedro de la Paz", "Santa Juana", "Talcahuano", "Tomé")),
            new ProvinciaData("Arauco", Arrays.asList("Lebu", "Arauco", "Cañete", "Contulmo", "Curanilahue", "Los Álamos", "Tirúa")),
            new ProvinciaData("Biobío", Arrays.asList("Los Ángeles", "Antuco", "Cabrero", "Laja", "Mulchén", "Nacimiento", "Negrete", "Quilaco", "Quilleco", "San Rosendo", "Santa Bárbara", "Tucapel", "Yumbel"))
        ));

        // La Araucanía
        poblarRegion(regionRepo, provinciaRepo, comunaRepo, "Región de La Araucanía", Arrays.asList(
            new ProvinciaData("Cautín", Arrays.asList("Temuco", "Carahue", "Cholchol", "Cunco", "Curarrehue", "Freire", "Galvarino", "Gorbea", "Lautaro", "Loncoche", "Melipeuco", "Nueva Imperial", "Padre Las Casas", "Perquenco", "Pitrufquén", "Pucón", "Saavedra", "Teodoro Schmidt", "Toltén", "Vilcún", "Villarrica")),
            new ProvinciaData("Malleco", Arrays.asList("Angol", "Collipulli", "Curacautín", "Ercilla", "Lonquimay", "Los Sauces", "Lumaco", "Purén", "Renaico", "Traiguén", "Victoria"))
        ));

        // Los Ríos
        poblarRegion(regionRepo, provinciaRepo, comunaRepo, "Región de Los Ríos", Arrays.asList(
            new ProvinciaData("Valdivia", Arrays.asList("Valdivia", "Corral", "Lanco", "Los Lagos", "Máfil", "Mariquina", "Paillaco", "Panguipulli")),
            new ProvinciaData("Ranco", Arrays.asList("Futrono", "La Unión", "Lago Ranco", "Río Bueno"))
        ));

        // Los Lagos
        poblarRegion(regionRepo, provinciaRepo, comunaRepo, "Región de Los Lagos", Arrays.asList(
            new ProvinciaData("Llanquihue", Arrays.asList("Puerto Montt", "Calbuco", "Cochamó", "Fresia", "Frutillar", "Los Muermos", "Llanquihue", "Maullín", "Puerto Varas")),
            new ProvinciaData("Chiloé", Arrays.asList("Castro", "Ancud", "Chonchi", "Curaco de Vélez", "Dalcahue", "Puqueldón", "Queilén", "Quellón", "Quemchi", "Quinchao")),
            new ProvinciaData("Osorno", Arrays.asList("Osorno", "Puerto Octay", "Purranque", "Puyehue", "Río Negro", "San Juan de la Costa", "San Pablo")),
            new ProvinciaData("Palena", Arrays.asList("Chaitén", "Futaleufú", "Hualaihué", "Palena"))
        ));

        // Aysén
        poblarRegion(regionRepo, provinciaRepo, comunaRepo, "Región Aysén del General Carlos Ibáñez del Campo", Arrays.asList(
            new ProvinciaData("Coyhaique", Arrays.asList("Coyhaique", "Lago Verde")),
            new ProvinciaData("Aysén", Arrays.asList("Aysén", "Cisnes", "Guaitecas")),
            new ProvinciaData("Capitán Prat", Arrays.asList("Cochrane", "O'Higgins", "Tortel")),
            new ProvinciaData("General Carrera", Arrays.asList("Chile Chico", "Río Ibáñez"))
        ));

        // Magallanes
        poblarRegion(regionRepo, provinciaRepo, comunaRepo, "Región de Magallanes y de la Antártica Chilena", Arrays.asList(
            new ProvinciaData("Magallanes", Arrays.asList("Punta Arenas", "Laguna Blanca", "Río Verde", "San Gregorio")),
            new ProvinciaData("Antártica Chilena", Arrays.asList("Cabo de Hornos", "Antártica")),
            new ProvinciaData("Tierra del Fuego", Arrays.asList("Porvenir", "Primavera", "Timaukel")),
            new ProvinciaData("Última Esperanza", Arrays.asList("Natales", "Torres del Paine"))
        ));
    }

    private void poblarComunidadesEspecificas(
        RegionRepository regionRepo,
        ProvinciaRepository provinciaRepo,
        ComunaRepository comunaRepo,
        ComunidadRepository comunidadRepo
    ) {
        // Buscar la región Metropolitana de Santiago
        Region regionMetropolitana = regionRepo.findByNombre("Región Metropolitana de Santiago");
        if (regionMetropolitana == null) {
            System.out.println("❌ Región Metropolitana de Santiago no encontrada");
            return;
        }

        // Buscar la provincia Santiago
        Provincia provinciaSantiago = provinciaRepo.findByNombre("Santiago");
        if (provinciaSantiago == null) {
            System.out.println("❌ Provincia Santiago no encontrada");
            return;
        }

        // Buscar las comunas
        Comuna comunaLaFlorida = comunaRepo.findByNombre("La Florida");
        Comuna comunaRecoleta = comunaRepo.findByNombre("Recoleta");
        Comuna comunaSantiago = comunaRepo.findByNombre("Santiago");

        if (comunaLaFlorida == null) {
            System.out.println("❌ Comuna La Florida no encontrada");
        } else {
            // Crear comunidad Condominio Bosques de la Florida
            crearComunidadSiNoExiste(comunidadRepo, "Condominio Bosques de la Florida", comunaLaFlorida);
        }

        if (comunaRecoleta == null) {
            System.out.println("❌ Comuna Recoleta no encontrada");
        } else {
            // Crear comunidad Condominio Los Recoletos
            crearComunidadSiNoExiste(comunidadRepo, "Condominio Los Recoletos", comunaRecoleta);
        }

        if (comunaSantiago == null) {
            System.out.println("❌ Comuna Santiago no encontrada");
        } else {
            // Crear comunidad Provisoria
            crearComunidadSiNoExiste(comunidadRepo, "Provisoria", comunaSantiago);
        }
    }

    private void poblarRegion(
        RegionRepository regionRepo,
        ProvinciaRepository provinciaRepo,
        ComunaRepository comunaRepo,
        String nombreRegion,
        List<ProvinciaData> provinciasData
    ) {
        Region region = regionRepo.findByNombre(nombreRegion);
        if (region == null) {
            System.out.println("Región no encontrada: " + nombreRegion);
            return;
        }

        for (ProvinciaData provinciaData : provinciasData) {
            // Crear provincia
            Provincia provincia = provinciaRepo.findByNombre(provinciaData.nombre);
            if (provincia == null) {
                provincia = new Provincia();
                provincia.setNombre(provinciaData.nombre);
                provincia.setCodigo(provinciaData.nombre.toLowerCase().replaceAll("\\s+", "-"));
                provincia.setRegion(region);
                provincia = provinciaRepo.save(provincia);
                System.out.println("Provincia creada: " + provinciaData.nombre + " en " + nombreRegion);
            }

            // Crear comunas
            for (String nombreComuna : provinciaData.comunas) {
                Comuna comuna = comunaRepo.findByNombre(nombreComuna);
                if (comuna == null) {
                    comuna = new Comuna();
                    comuna.setNombre(nombreComuna);
                    comuna.setCodigo(nombreComuna.toLowerCase().replaceAll("\\s+", "-"));
                    comuna.setProvincia(provincia);
                    comunaRepo.save(comuna);
                    System.out.println("Comuna creada: " + nombreComuna + " en " + provinciaData.nombre);
                }
            }
        }
    }

    private void crearComunidadSiNoExiste(ComunidadRepository comunidadRepo, String nombreComunidad, Comuna comuna) {
        Comunidad comunidad = comunidadRepo.findByNombreComunidad(nombreComunidad);
        if (comunidad == null) {
            comunidad = new Comunidad();
            comunidad.setNombreComunidad(nombreComunidad);
            comunidad.setComuna(comuna);
            comunidadRepo.save(comunidad);
            System.out.println("✅ Comunidad creada: " + nombreComunidad + " en " + comuna.getNombre());
        } else {
            System.out.println("ℹ️ Comunidad ya existe: " + nombreComunidad);
        }
    }

    // Clase auxiliar para estructurar los datos de provincias y comunas
    private static class ProvinciaData {
        String nombre;
        List<String> comunas;

        ProvinciaData(String nombre, List<String> comunas) {
            this.nombre = nombre;
            this.comunas = comunas;
        }
    }
}
