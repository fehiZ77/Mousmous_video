"use client"

import { useEffect, useState, useRef } from "react"
import { usePathname } from "next/navigation"
import { Spinner } from "@/components/ui/spinner"

export function CustomLoadingBar() {
  const [isLoading, setIsLoading] = useState(false)
  const pathname = usePathname()
  const prevPathnameRef = useRef(pathname)
  const timeoutRef = useRef<NodeJS.Timeout | null>(null)

  useEffect(() => {
    // Détecter les changements de route
    if (pathname !== prevPathnameRef.current) {
      // Nettoyer le timeout précédent s'il existe
      if (timeoutRef.current) {
        clearTimeout(timeoutRef.current)
      }

      setIsLoading(true)
      prevPathnameRef.current = pathname
      
      // Masquer le loader après un court délai
      timeoutRef.current = setTimeout(() => {
        setIsLoading(false)
      }, 300)
    }

    return () => {
      if (timeoutRef.current) {
        clearTimeout(timeoutRef.current)
      }
    }
  }, [pathname])

  // Détecter les clics sur les liens Next.js
  useEffect(() => {
    const handleClick = (e: MouseEvent) => {
      const target = e.target as HTMLElement
      const link = target.closest("a")
      
      // Vérifier si c'est un lien Next.js (commence par / et n'est pas un hash)
      if (link && link.href && link.getAttribute("href")?.startsWith("/") && !link.getAttribute("href")?.startsWith("#")) {
        setIsLoading(true)
        
        // Le loader sera masqué par le changement de pathname
        if (timeoutRef.current) {
          clearTimeout(timeoutRef.current)
        }
        
        timeoutRef.current = setTimeout(() => {
          setIsLoading(false)
        }, 500)
      }
    }

    document.addEventListener("click", handleClick, true)

    return () => {
      document.removeEventListener("click", handleClick, true)
      if (timeoutRef.current) {
        clearTimeout(timeoutRef.current)
      }
    }
  }, [])

  if (!isLoading) return null

  return (
    <div className="fixed bottom-4 left-4 z-50 flex items-center gap-2 rounded-lg bg-card border border-border px-4 py-2.5 shadow-lg animate-in fade-in slide-in-from-bottom-2 duration-200">
      <Spinner className="w-4 h-4 text-primary" />
      <span className="text-sm font-medium text-foreground">Chargement...</span>
    </div>
  )
}
