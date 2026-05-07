package com.emsi.marches_backend.config;

import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.ws.transport.http.MessageDispatcherServlet;
import org.springframework.ws.wsdl.wsdl11.DefaultWsdl11Definition;
import org.springframework.xml.xsd.SimpleXsdSchema;
import org.springframework.xml.xsd.XsdSchema;

/**
 * Configuration pour les Web Services SOAP
 * Active les endpoints SOAP et configure le dispatcher
 */
// @Configuration
// @EnableWs
// Désactivé temporairement - fichier offre.xsd manquant
// À réactiver quand le fichier sera disponible
public class WebServiceConfig {

    /**
     * Configure le servlet SOAP dispatcher
     * Mappe les requêtes /ws/* vers le MessageDispatcherServlet
     */
    // @Bean
    public ServletRegistrationBean<MessageDispatcherServlet> messageDispatcherServlet(ApplicationContext applicationContext) {
        MessageDispatcherServlet servlet = new MessageDispatcherServlet();
        servlet.setApplicationContext(applicationContext);
        servlet.setTransformWsdlLocations(true);
        return new ServletRegistrationBean<>(servlet, "/ws/*");
    }

    /**
     * Définit le WSDL par défaut pour l'endpoint offre
     * Accessible à: http://localhost:8080/ws/offre.wsdl
     */
    // @Bean(name = "offre")
    public DefaultWsdl11Definition defaultWsdl11Definition(XsdSchema offreSchema) {
        DefaultWsdl11Definition wsdl11Definition = new DefaultWsdl11Definition();
        wsdl11Definition.setPortTypeName("OffrePort");
        wsdl11Definition.setLocationUri("/ws/offre");
        wsdl11Definition.setTargetNamespace("http://marches-backend.emsi.com/ws");
        wsdl11Definition.setSchema(offreSchema);
        return wsdl11Definition;
    }

    /**
     * Charge le schéma XSD pour validation
     */
    // @Bean
    public XsdSchema offreSchema() {
        return new SimpleXsdSchema(new org.springframework.core.io.ClassPathResource("offre.xsd"));
    }
}
