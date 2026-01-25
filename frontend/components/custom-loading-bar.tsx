"use client"

import { useEffect, useState, useRef } from "react"
import { usePathname } from "next/navigation"

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

  useEffect(() => {
    // Appliquer ou retirer la transparence à la page pendant le chargement
    if (isLoading) {
      document.body.style.opacity = "0.5"
      document.body.style.pointerEvents = "none"
    } else {
      document.body.style.opacity = "1"
      document.body.style.pointerEvents = "auto"
    }

    return () => {
      document.body.style.opacity = "1"
      document.body.style.pointerEvents = "auto"
    }
  }, [isLoading])

  if (!isLoading) return null

  return (
    <>
      <style>{`
        @keyframes ht {
          100% { height: 0px }
        }
        
        .loader {
          height: 30px;
          width: 10px;
          border-radius: 4px;
          color: #fff;
          background: currentColor;
          position: relative;
          animation: ht 1s ease-in infinite alternate;
          box-shadow: 15px 0 0 -1px, -15px 0 0 -1px,
                      30px 0 0 -2px, -30px 0 0 -2px,
                      45px 0 0 -3px, -45px 0 0 -3px;
        }
      `}</style>
      <div className="fixed inset-0 z-50 flex items-center justify-center bg-transparent">
        <span className="loader"></span>
      </div>
    </>
  )
}
